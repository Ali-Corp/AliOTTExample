    //
    //  AppDelegate.swift
    //  ALIOTTExample
    //
    //  Created by Vu Ho on 11/6/24.
    //

import UIKit
import SwiftyJSON
import ALIOTT
import PushKit

typealias NotificationPayload = [AnyHashable: Any]
typealias FetchCompletion = (UIBackgroundFetchResult) -> Void

class AppDelegate: UIResponder, UIApplicationDelegate {
    public static var shared: AppDelegate? = nil

    private var voipRegistry: PKPushRegistry?
    private var pushKitToken: String = ""

    public var callerId: String = ""
    public var calleeId: String = ""

    var onRequestShowCall: ((_ call: ALIOTTCall) -> Void)? = nil

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        AppDelegate.shared = self
        let callKitConfig = CallKitConfig(
            iconTemplateImageData: UIImage(named: "AppIcon")?.pngData(),
            ringtoneSound: "custom_sound.mp3")

        var customIncomingCallSoundUrl: URL? = nil
        if let path = Bundle.main.path(forResource: "custom_sound", ofType: "mp3"),
           let url = Bundle.main.url(forAuxiliaryExecutable: path) {
            customIncomingCallSoundUrl = url
        }

        var customOutgoingCallSoundUrl: URL? = nil
        if let path = Bundle.main.path(forResource: "custom_sound", ofType: "mp3"),
           let url = Bundle.main.url(forAuxiliaryExecutable: path) {
            customOutgoingCallSoundUrl = url
        }

        ALIOTT.shared().config(
            environment: .sandbox,
            callKitConfig: callKitConfig,
            customIncomingCallSoundUrl: customIncomingCallSoundUrl,
            customOutgoingCallSoundUrl: customOutgoingCallSoundUrl)
        ALIOTT.shared().delegate = self

        voipRegistry = PKPushRegistry(queue: DispatchQueue.main)
        voipRegistry!.desiredPushTypes = [.voIP]
        voipRegistry!.delegate = self

        return true
    }

    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        return true
    }
}

extension AppDelegate: PKPushRegistryDelegate {
    func pushRegistry(_ registry: PKPushRegistry, didUpdate pushCredentials: PKPushCredentials, for type: PKPushType) {
        ALIOTT.shared().pushRegistry(registry, didUpdate: pushCredentials, for: type)
    }

    func pushRegistry(_ registry: PKPushRegistry, didReceiveIncomingPushWith payload: PKPushPayload, for type: PKPushType) {
        ALIOTT.shared().pushRegistry(registry, didReceiveIncomingPushWith: payload, for: type)
    }

    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
        ALIOTT.shared().pushRegistry(registry, didInvalidatePushTokenFor: type)
    }
}

extension AppDelegate: ALIOTTDelegate {
    func aliottOnRequestCustomMetadataForCall(call: ALIOTTCall) -> [String : Any] {
        return [
            "message_deeplink": "app-settings:root=General&path=ACCESSIBILITY"
        ]
    }
    
    func aliottOnUpdatePushKitToken(_ pushToken: String) {
        self.pushKitToken = pushToken
    }

    func aliottOnNotifyOutgoingCall(type: Int, alert: String, metadata: [String: Any]) {
        notifyCall(calleeId, alert: alert)
    }

    func aliottOnRequestShowCall(call: ALIOTTCall) {
        onRequestShowCall?(call)
    }
}

extension AppDelegate {
    func updatePushKitToken(_ userId: String) {
        let jsonData = try? JSONSerialization.data(withJSONObject: [
            "user_id": userId,
            "token": pushKitToken,
            "platform": "ios"
        ], options: .prettyPrinted)
        let request = NSMutableURLRequest(url: URL(string: "\(Env.host)/fcm/save")!, cachePolicy: .useProtocolCachePolicy, timeoutInterval: 20)
        request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData
        request.httpMethod = "POST"
        let dataTask = URLSession.shared.dataTask(with: request as URLRequest) {data,response,error in
            do {
                if let data = data,
                   let jsonResult = try JSONSerialization.jsonObject(with: data, options: []) as? NSDictionary {
                    let data = JSON(jsonResult)
                    debugPrint("updatePushKitToken success: ", data.debugDescription)
                }
            } catch let error as NSError {
                debugPrint("updatePushKitToken error: ", error.debugDescription)
            }
        }
        dataTask.resume()
    }

    func notifyCall(_ calleeId: String, alert: String) {
        let jsonData = try? JSONSerialization.data(withJSONObject: [
            "user_id": calleeId,
            "alert": alert,
            "platform": "ios"
        ], options: .prettyPrinted)

        let request = NSMutableURLRequest(url: URL(string: "\(Env.host)/ott/make_call")!, cachePolicy: .useProtocolCachePolicy, timeoutInterval: 20)
        request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData
        request.httpMethod = "POST"
        let dataTask = URLSession.shared.dataTask(with: request as URLRequest) {data,response,error in
            do {
                if let data = data,
                   let jsonResult = try JSONSerialization.jsonObject(with: data, options: []) as? NSDictionary {
                    let data = JSON(jsonResult)
                    debugPrint("notifyCall success: ", data.debugDescription)
                }
            } catch let error as NSError {
                debugPrint("notifyCall error: ", error.debugDescription)
            }
        }
        dataTask.resume()
    }
}
