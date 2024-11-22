    //
    //  ContentView.swift
    //  ALIOTTExample
    //
    //  Created by Vu Ho on 11/6/24.
    //

import SwiftUI
import ALIOTT

struct ContentView: View {
    @State var callerId = ""
    @State var calleeId = ""
    @State var loggedIn = false
    @State var hotline = ALIOTTHotlineConfig(id: Env.SERVICE_ID,
                                             key: Env.SERVICE_KEY)

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                HStack(spacing: 13) {
                    TextField(NSLocalizedString("lb_caller", comment: "Caller"), text: $callerId)
                        .keyboardType(.phonePad)
                        .disabled(loggedIn)
                        .padding()
                        .overlay(RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.blue, lineWidth: 1))
                    Button(loggedIn ? NSLocalizedString("btn_logout", comment: "Logout") :
                            NSLocalizedString("btn_login", comment: "Login")) {
                        if !loggedIn {
                            if !callerId.isEmpty {
                                loggedIn = true
                                ALIOTT.shared().login(
                                    withUser: ALIOTTUser(userId: callerId,
                                                         config: ALIOTTCallConfig(username: Env.username,
                                                                                  credential: Env.credential,
                                                                                  secret: Env.secret)))
                                AppDelegate.shared?.callerId = callerId
                                AppDelegate.shared?.updatePushKitToken(callerId)
                            }
                        } else {
                            loggedIn = false
                            ALIOTT.shared().logout()
                        }
                    }
                }

                if loggedIn {
                    HStack(spacing: 13) {
                        TextField(NSLocalizedString("lb_callee", comment: "Callee"), text: $calleeId)
                            .keyboardType(.phonePad)
                            .padding()
                            .overlay(RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.blue, lineWidth: 1))
                        Button("Gọi") {
                            if !calleeId.isEmpty {
                                UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                                AppDelegate.shared?.calleeId = calleeId
                                ALIOTT.shared().startOutgoingCall(
                                    handle: "Nguyễn Thị Nữ",
                                    callData: ALIOTTCallData(
                                        callerId: callerId,
                                        callerAvatar: "https://cdn-icons-png.flaticon.com/256/4825/4825015.png",
                                        callerName: "Nguyễn Văn Nam",
                                        calleeId: calleeId,
                                        calleeAvatar: "https://cdn-icons-png.flaticon.com/256/147/147144.png",
                                        calleeName: "Nguyễn Thị Nữ",
                                        type: 1,
                                        metadata: [
                                            "check_sum": "5270369466588474968f1730711963000"
//                                            "check_sum": "5270369466588474968f17307119"
                                        ]
                                ))
                            }
                        }
                    }

                    if hotline.name != "" {
                        Button {
                            UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                            ALIOTT.shared().startHotlineCall(
                                hotlineConfig: hotline,
                                callerId: callerId,
                                callerName: "Nguyễn Văn Nam",
                                callerAvatar: "https://cdn-icons-png.flaticon.com/256/4825/4825015.png",
                                metadata: [:]
                            )
                        } label: {
                            VStack(spacing: 12) {
                                Image(systemName: "phone.fill")
                                    .imageScale(.large)
                                Text(NSLocalizedString("btn_call_hotline", comment: "Call hotline"))
                                Text(hotline.name)
                            }
                        }
                    }
                }
            }
            .padding()
        }
        .onAppear {
            ALIOTT.shared().initHotline(hotlineConfig: hotline)
            { initedHotlineConfig, error in
                if let initedHotlineConfig = initedHotlineConfig {
                    self.hotline = initedHotlineConfig
                }
            }
        }
    }
}

#Preview {
    ContentView()
}
