//
//  HotlineCallExampleApp.swift
//  HotlineCallExample
//
//  Created by Vu Ho on 11/6/24.
//

import SwiftUI
import ALIOTTHotline
import SwiftyJSON

@main
struct HotlineCallExampleApp: App {
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
                    }
                if let call = call {
                    NavigationView {
                        CallView(call: call) {
                            self.call = nil
                        }
                    }
                }
            }
        }
        .onChange(of: scenePhase) { phase in
        }
    }
}
