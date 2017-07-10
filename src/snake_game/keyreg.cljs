(ns snake-game.keyreg
  (:require [re-frame.core :refer [dispatch reg-event-db]]
            [goog.events :as events]
            [snake-game.util :as util]))

; The game allows you to register handlers for key- game-state commbinations..
; - Use reg-key-bindings to register events for key and game-state combinations
; - Use update-game-state to set the current game state directly
; - Use the :change-game-state event to set the new gamestate through events.


(comment {:key-handlers {:running [[37 :sn-up]
                                   [38 :sn-right]
                                   [32 :sn-pause]]
                         :loaded  [[37 :menu-up]
                                   [:39 :menu-down]]}})

(enable-console-print!)
(defn log [& strs]
  (.log js/console (reduce str (interleave strs (repeat " ")))))

;
; GAME CONFIG
;
(defonce game-config (atom {:key-handlers {}}))


(defn reg-key-bindings
  [game-state key-code event up-down]
  (util/log "Register event: " event " to game state: " game-state " and key code: " key-code " action " up-down)
  (swap! game-config (fn [m]
                       (update-in m
                                  [:key-handlers game-state]
                                  #(into [] (conj % [key-code event up-down]))))))


(defn- dispatch-events-for-key [key-code state-handlers]
  (if-let [handlers-for-key (seq (filter #(= key-code (first %1)) state-handlers))]
    (do
      (doseq [[_ event] handlers-for-key] (dispatch [event]))
      true)
    false))


;
; EVENT HANDLERS
;
(defn handle-key-event [db event up-down]
  (let [key-code (.-keyCode event)
        game-state (:game-state db)
        state-handlers (->>
                         (get (:key-handlers @game-config) game-state)
                         (filter (fn [[_ _ ud]] (= ud up-down))))]
    (if (and
          (seq state-handlers)
          (dispatch-events-for-key key-code state-handlers))
      (do
        (util/log "Handler(s) found for " event " and event type " up-down)
        (.preventDefault event))
      (util/log "WARING! no key hander found for key-code " key-code " and game state " game-state "and event type " up-down))))


(reg-event-db
  :keydown-event
  (fn [db [_ event]]
    (handle-key-event db event :down)
    db))

(reg-event-db
  :keyup-event
  (fn [db [_ event]]
    (handle-key-event db event :up)
    db))







(defonce key-handler-down
         (events/listen js/window "keydown"
                        (fn [e]
                          (dispatch [:keydown-event e]))))

(defonce key-handler-up
         (events/listen js/window "keyup"
                        (fn [e]
                          (dispatch [:keyup-event e]))))
