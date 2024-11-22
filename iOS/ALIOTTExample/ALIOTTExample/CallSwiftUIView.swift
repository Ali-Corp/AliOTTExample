    //
    //  CallView.swift
    //  ALIOTTExample
    //
    //  Created by Vu Ho on 11/6/24.
    //
import Foundation
import SwiftUI
import SwiftyJSON
import ALIOTT

struct CallSwiftUIViewRepresentable: UIViewRepresentable {
    let call: ALIOTTCall

    func makeUIView(context: Context) -> ALIOTTCallView {
            // Instantiate and configure your CallView
        let callView = ALIOTTCallView(call: call)
            // Additional setup if needed
        return callView
    }

    func updateUIView(_ uiView: ALIOTTCallView, context: Context) {
            // Update the view when SwiftUI state changes, if needed
    }
}

    // Step 2: Use CallSwiftUIView in SwiftUI
struct CallSwiftUIView: View {
    @Environment(\.presentationMode) private var presentationMode
    @State private var call: ALIOTTCall

    init(call: ALIOTTCall) {
        _call = .init(initialValue: call)
    }

    var body: some View {
        CallSwiftUIViewRepresentable(call: call)
            .id(call.uuid.uuidString)
            .ignoresSafeArea()
    }
}

struct CallSwiftUIView_Previews: PreviewProvider {
    static var previews: some View {
        CallSwiftUIView(
            call: ALIOTTCall(uuid: UUID(),
                             handle: "Nguyễn Vân Nam",
                             callerId: "",
                             callerAvatar: "https://cdn-icons-png.flaticon.com/256/4825/4825112.png",
                             callerName: "Nguyễn Vân Nam",
                             calleeId: "",
                             calleeAvatar: "",
                             calleeName: "",
                             metadata: [
                                "message_deeplink": "app-settings:root=General&path=ACCESSIBILITY"
                             ],
                             type: 1,
                             outgoing: true,
                             hotline: false,
                             state: .ended(.calleeEnd),
                             connectedState: .connected,
                             connectedTime: nil))
    }
}
