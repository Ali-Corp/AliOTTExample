//
//  AppDelegate.swift
//  HotlineCallExample
//
//  Created by Vu Ho on 11/6/24.
//

import UIKit
import SwiftyJSON
import ALIOTTHotline

#if ENABLE_UITUNNEL
//import SBTUITestTunnelServer
#endif

typealias NotificationPayload = [AnyHashable: Any]
typealias FetchCompletion = (UIBackgroundFetchResult) -> Void

class AppDelegate: UIResponder, UIApplicationDelegate {
    var onRequestShowCallView: ((_ call: ALIOTTCall) -> Void)? = nil
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        ALIOTT.shared().delegate = self
        ALIOTT.shared().config(
            environment: .production,
            hotlineConfig: ALIOTTHotlineConfig(id: "SERVICE_ID",
                                               key: "SERVICE_KEY",
                                               name: "SERVICE_NAME",
                                               icon: "SERVICE_AVATAR"),
            callKitConfig: CallKitConfig(iconTemplateImageData: UIImage(named: "AppIcon")?.pngData(),
                                         ringtoneSound: nil))
        
        return true
    }
    
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        return true
    }
}

extension AppDelegate: ALIOTTDelegate {
    func aliottOnInitSuccess() {
        debugPrint("aliottOnInitSuccess")
    }
    
    func aliottOnInitFail() {
        debugPrint("aliottOnInitFail")
    }
    
    func aliottOnRequestShowCallView(call: ALIOTTCall) {
        onRequestShowCallView?(call)
    }
}
