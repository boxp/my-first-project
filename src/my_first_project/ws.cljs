(ns my-first-project.ws
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [close! put! <! chan]]))

(def url "ws://localhost:8080/ws")
(def ws (js/WebSocket. url))

(defprotocol ILifeCycle
  (start [this])
  (stop [this]))

(defrecord WebSocket [receive]
  ILifeCycle
  ;; WebSocketを起動
  (start [this]
    (let [receive (chan)]
      ;; メッセージ受信時の処理
      (set! (. ws -onmessage) #(put! receive %))
      ;; 新しいClojureオブジェクトを返す
      (assoc this :receive receive)))
  ;; WebSocketを終了
  (stop [this]
    (close! (:receive this))))

(defn open []
  (-> (->WebSocket nil)
      (start)))

(defn close 
  [ws]
  (stop ws))

(defn send 
  [message]
  (.send ws message))
