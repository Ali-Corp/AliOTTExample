//
//  CallView.swift
//  HotlineCallExample
//
//  Created by Vu Ho on 11/6/24.
//
import Foundation
import SwiftUI
import SwiftyJSON
import ALIOTTHotline

struct CallView: View {
    @Environment(\.presentationMode) private var presentationMode
    
    @State private var avatarImage: UIImage? = nil
    
    @State private var call: ALIOTTCall
    @State private var speakerOn: Bool
    @State private var mute: Bool
    @State private var callState: ALIOTTCallState
    @State private var connectedState: ALIOTTConnectedState?
    @State private var connectedDate: Date?
    @State private var inCallDuration: String = ""
    
    private var onDismiss: () -> Void
    
    private let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
    
    init(call: ALIOTTCall, onDismiss: @escaping () -> Void) {
        _call = .init(initialValue: call)
        _speakerOn = .init(initialValue: true)
        _mute = .init(initialValue: false)
        _callState = .init(initialValue: .calling)
        self.onDismiss = onDismiss
    }
    
    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()
            
            VStack {
                VStack(spacing: 13) {
                    if #available(iOS 15.0, *) {
                        AsyncImage(url: URL(string: call.calleeAvatar)) { image in
                            image.resizable()
                        } placeholder: {
                            Image("avatar_placeholder")
                        }
                        .frame(width: 100, height: 100)
                        .cornerRadius(40)
                        .background(Circle().fill(.white))
                        .clipShape(Circle())
                    } else {
                        Group {
                            if avatarImage == nil {
                                Image("avatar_placeholder")
                            } else {
                                Image(uiImage: avatarImage!)
                            }
                        }
                        .frame(width: 100, height: 100)
                        .cornerRadius(40)
                        .background(Circle().fill(.white))
                        .clipShape(Circle())
                        .onAppear {
                            if let url = URL(string: call.calleeAvatar) {
                                URLSession.shared.dataTask(with: URLRequest(url: url)) { data, _, error in
                                    DispatchQueue.main.async {
                                        if let error = error {
                                            return
                                        }
                                        
                                        if let data = data {
                                            self.avatarImage = UIImage(data: data)
                                        }
                                    }
                                }
                                .resume()
                            }
                        }
                    }
                    
                    Text(call.handle)
                        .foregroundColor(Color.white)
                    
                    switch callState {
                    case .pending:
                        Text("Calling...")
                            .font(.body)
                            .foregroundColor(Color.green)
                    case .calling:
                        Text("Calling...")
                            .font(.body)
                            .foregroundColor(Color.green)
                    case .active:
                        if connectedDate == nil {
                            Text("Calling...")
                                .font(.body)
                                .foregroundColor(Color.green)
                        } else {
                            Text(inCallDuration)
                                .font(.body)
                                .foregroundColor(Color.white)
                        }
                    case .ended:
                        Text("Call Ended")
                            .font(.body)
                            .foregroundColor(Color.red)
                    }
                }
                
                Spacer()
                
                if callState == .active && connectedDate != nil {
                    HStack(spacing: 40) {
                        Button {
                            mute = !mute
                            ALIOTT.shared().setMuteCall(mute: mute)
                        } label: {
                            ZStack {
                                Rectangle()
                                    .fill(.clear)
                                    .frame(minWidth: 0, maxWidth: .infinity)
                                VStack(spacing: 20) {
                                    Image(systemName: mute ? "mic.slash.circle" : "mic.circle.fill")
                                        .font(.system(size: 60, weight: .thin))
                                    Text("Mute")
                                }
                                .foregroundColor(.white)
                            }
                        }
                        .fixedSize(horizontal: false, vertical: true)
                        
                        Button {
                            speakerOn = !speakerOn
                            ALIOTT.shared().setSpeakOn(on: speakerOn)
                        } label: {
                            ZStack {
                                Rectangle()
                                    .fill(.clear)
                                    .frame(minWidth: 0, maxWidth: .infinity)
                                VStack(spacing: 20) {
                                    Image(systemName: speakerOn ? "speaker.wave.2.circle.fill" : "speaker.wave.2.circle")
                                        .font(.system(size: 60, weight: .thin))
                                    Text("Speaker")
                                }
                                .foregroundColor(.white)
                            }
                        }
                        .fixedSize(horizontal: false, vertical: true)
                    }
                    .frame(height: 100)
                    .padding(20)
                }
                
                Button {
                    callState = .ended
                    ALIOTT.shared().endCall()
                } label: {
                    ZStack {
                        Color.red
                            .opacity(callState == .ended ? 0.8 : 1)
                            .frame(width: 80, height: 80)
                            .clipShape(Circle())
                        Image(systemName: "phone.down.fill")
                            .foregroundColor(.white)
                            .font(.system(size: 32))
                    }
                }
            }
            .padding(20)
        }
        .navigationBarHidden(true)
//        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if callState == .ended {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    dismiss()
                }
            }
            ALIOTT.shared().callDelegate = self
        }
        .onDisappear {
            ALIOTT.shared().callDelegate = nil
        }
        .onReceive(timer) { _ in
            var dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "mm:ss"   // HH for 24h clock

            if let connectedDate = connectedDate {
                let delta = connectedDate.timeIntervalSinceNow
                inCallDuration = dateFormatter.string(from: Date(timeIntervalSince1970: -delta))
            } else {
                inCallDuration = ""
            }
        }
        .onChange(of: callState) { newValue in
            if newValue == .ended {
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                    onDismiss()
                }
            }
        }
    }
    
    private func dismiss() {
        self.presentationMode.wrappedValue.dismiss()
    }
}

extension CallView: ALIOTTCallDelegate {
    func aliottOnCallStateChange(_ callState: ALIOTTCallState) {
        if self.callState != callState {
            self.callState = callState
        }
    }
    
    func aliottOnCallConnectedChange(_ connectedState: ALIOTTConnectedState?, connectedDate: Date?) {
        if self.connectedState != connectedState {
            self.connectedState = connectedState
        }
        
        if self.connectedDate == nil && connectedDate != nil {
            self.connectedDate = connectedDate
        }
    }
}
