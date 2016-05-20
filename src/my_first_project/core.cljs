(ns my-first-project.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [close! put! <! chan]]
            [secretary.core :as secretary :refer-macros [defroute]] ;; Changed!
            [goog.events :as events] ;; Changed!
            [goog.history.EventType :as EventType] ;; Changed!

            [my-first-project.ws :as ws])
  (:import goog.History)) ;; Changed!

(enable-console-print!)

(println "This text is printed from src/my-first-project/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

;; Appended!
(defn navigator [_ owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/a #js{:href "#/"}
               "Home")
        (dom/a #js{:href "#/chat"}
               "Chat")))))

(defn chat-view [_ owner]
  (reify 
    ;; Changed!
    om/IWillMount
    (will-mount [_]
      (go (loop []
            ;; $B%5!<%P!<$+$i%a%C%;!<%8$r<u$1<h$k(B
            (let [e (<! (om/get-state owner [:websocket :receive]))
                  message (if e (. e -data))]
              ;; $B%5!<%P!<$+$i<u$1<h$C$?%a%C%;!<%8$rDI2C(B
              (om/update-state! owner :messages #(conj % message))
              (println message)
              (when message
                (recur))))))
    om/IWillUnmount
    (will-unmount [_]
      ;; WebSocket$B$N@\B3$r=*N;$5$;$k(B
      (ws/close (om/get-state owner :websocket)))
    om/IInitState
    (init-state [_]
      {:messages []
       :websocket (ws/open)})
    om/IRenderState
    (render-state [_ state]
      (dom/div nil
        (om/build navigator {}) ;; Changed!
        (dom/label #js{:for "message"} 
                   "message: ")
        (dom/input #js{:type "text" :ref "message"})
        (dom/br nil)
        (dom/label #js{:for "send"} 
                   "send message: ")
        (dom/button #js{:id "send"
                        :onClick #(ws/send (. (om/get-node owner "message") -value))}
                    "send")
        (dom/ul nil 
          (map #(dom/li nil %) (:messages state)))))))

(defn home-view [data owner]
  (reify om/IRender
    (render [_]
      (dom/div nil
        (om/build navigator {}) ;; Changed!
        (dom/h1 nil (:text data))))))

;; Comment out!!
;;(om/root 
;;  chat-view 
;;  app-state
;;  {:target (. js/document (getElementById "app"))})

;; http://localhost:3449/$B0J2<$N(BURL$B$N(Bprefix$B$r;XDj(B
(secretary/set-config! :prefix "#") ;; Appended!

;; Appended!
(defroute chat-path "/chat" []
  (om/root
    chat-view
    {}
    {:target (. js/document (getElementById "app"))}))

;; Appended!
(defroute home-path "/" []
  (om/root
    home-view
    app-state
    {:target (. js/document (getElementById "app"))}))

;; URL$B$NJQ99$r8!CN$7!"%k!<%F%#%s%0$r9T$&(B
(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
