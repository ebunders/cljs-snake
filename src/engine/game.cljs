(ns engine.game
  (:require [re-frame.core :refer [reg-event-db dispatch-sync dispatch]]
            [goog.events :as events]))
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
  [game-state key-code event]
    (swap! game-config (fn [m]
                         (update-in m
                                    [:key-handlers game-state]
                                    #(into [] (conj % [key-code event]))))))


(defn- dispatch-events-for-key [key-code state-handlers]
  "Invokes the handlers for this keycode and this state
  Returns true if some handler was executed for this key in this game state"
  (if-let [handlers-for-key (seq (filter #(= key-code (first %1)) state-handlers))]
    (do
      (doseq [[_ event] handlers-for-key] (dispatch [event]))
      true)
    false))


;
; EVENT HANDLERS
;

(reg-event-db
  :keydown-event
  (fn [db [_ event]]
    (let [key-code (.-keyCode event)
          game-state (:game-state db)
          state-handlers (get (:key-handlers @game-config) game-state)]
      (if (and
            (seq? state-handlers)
            (dispatch-events-for-key key-code state-handlers))
        (.preventDefault event)))))




(defonce key-handler
         (events/listen js/window "keydown"
                        (fn [e]
                          (dispatch [:keydown-event e]))))