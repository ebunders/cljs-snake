(ns snake-game.game.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]
            [snake-game.game.data :as data]
            [snake-game.game.logic :as logic]
            [snake-game.util :as util]))

(reg-event-db :start-game
              (fn [db _]
                (let [speeds [150 125 100]
                      game-speed (get speeds (dec (:game-level db)))]
                  (-> db
                      (assoc :jsinterval (js/setInterval #(dispatch [:next-state]) game-speed))
                      (assoc :game-state :state-running)))
                ))

(reg-event-db :stop-game
              (fn [db _]
                (js/clearInterval (:jsinterval db))
                (dissoc db :jsinterval)))


(reg-event-fx
  :next-state
  (fn [cofx event]
    (let [db (:db cofx)
          snake (:snake db)
          board (:board db)
          snake-state (:snake-state snake)
          running-and-moving (and (data/game-is-running? db) (data/snake-is-moving? db))
          update-game (fn [db]
                        (-> db
                            (update-in [:snake] logic/move-snake)
                            (as-> after-move
                                  (logic/process-move after-move))))]
      (if running-and-moving
        (do
          (util/log "running and moving!!!!")
          (if (logic/collisions snake board)
            {:db             (assoc-in db [:snake :snake-state] :snake-collided)
             :dispatch-later [{:ms 200 :dispatch [:blink 5 200]}]}
            {:db (update-game db)}))                         ;; no collisions - move the game forward)
          {:db db}))))                                      ;; not running and moving- no change



  (reg-event-fx
    :blink
    (fn [cofx [_ counter interval]]
      (let [db (:db cofx)]
        (if (> counter 0)
          (do
            ;(util/log "retrigger blink. counter: " counter)
            {:db             (update-in db [:snake :blink-head] not)
           :dispatch-later [{:ms interval :dispatch [:blink (dec counter) interval]}]})
          {:db (assoc db :game-state :state-finished)}))))


  (reg-event-db
    :change-direction
    (fn [db [_ new-direction]]

      (if (and (data/game-is-running? db) (data/snake-is-moving? db))

        (update-in db
                   [:snake :direction]
                   (partial logic/change-snake-direction new-direction))
        db)))


  (reg-event-db
    :toggle-pause
    (fn [db _]
      (let [new-state (if (data/game-is-paused? db) :state-running :state-paused)]
        (util/log ":toggel-pause. new state is:" new-state "game-is-paused:" (data/game-is-paused? db) "db:" db)
        (assoc db :game-state new-state))))


  (reg-event-db :key-up (fn [db _] (assoc-in db [:snake :direction] [-1 0])))
  (reg-event-db :key-right (fn [db _] (assoc-in db [:snake :direction] [0 -1])))
  (reg-event-db :key-down (fn [db _] (assoc-in db [:snake :direction] [1 0])))
  (reg-event-db :key-left (fn [db _] (assoc-in db [:snake :direction] [0 1])))


