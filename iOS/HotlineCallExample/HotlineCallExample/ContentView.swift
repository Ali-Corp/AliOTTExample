//
//  ContentView.swift
//  HotlineCallExample
//
//  Created by Vu Ho on 11/6/24.
//

import SwiftUI
import ALIOTTHotline

struct ContentView: View {
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
            Button {
                ALIOTT.shared().startHotlineCall(
                    callerId: "CALLER_ID",
                    callerName: "CALLER_NAME",
                    callerAvatar: "CALLER_AVATAR"
                )
            } label: {
                Text("Call hotline")
            }
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
