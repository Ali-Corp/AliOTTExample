//
//  ALIOTTExampleApp.swift
//  ALIOTTExample
//
//  Created by Vu Ho on 11/6/24.
//

import SwiftUI
import ALIOTT
import SwiftyJSON

@main
struct ALIOTTExampleApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @UIApplicationDelegateAdaptor var delegate: AppDelegate

    @State private var call: ALIOTTCall? = nil

    var body: some Scene {
        WindowGroup {
            ZStack {
                ContentView()
                    .onAppear {
                        delegate.onRequestShowCall = { call in
                            self.call = call
                        }
                        delegate.onRequestHideCall = { reason in
                            self.call = nil
                        }
                    }
                if let call = call {
                    NavigationView {
                        CallSwiftUIView(call: call)
                        .navigationBarHidden(true)
                    }
                }
            }
        }
        .onChange(of: scenePhase) { phase in
        }
    }
}
