(ns snake-game.game.view
  (:require
    [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
    [snake-game.game.data :as data]))

(defn render-snake [current-pos snake]
  (let [snake-head (first (:body snake))
        at-snake-head? (= current-pos snake-head)
        blinking? (:blink-head snake)]

    (if (and blinking? at-snake-head?)
      (do
        (.log js/console "current pos: " (str current-pos) " snake head:" (str snake-head) " blinking: " (str blinking?))
        [:div.cell.snake-on.blink])
      [:div.cell.snake-on])))

(defn render-point [] [:div.cell.point "*"])

(defn render-cell [] [:div.cell])


;dohere: create a reg-sub function that calculates the board, and only display it here.
(defn render-board []
  (let [board (subscribe [:board])
        snake (subscribe [:snake])
        point (subscribe [:point])
        game-state (subscribe [:game-state])]
    (fn []
      (if (some #{@game-state} [:state-running :state-paused])
        (let [[width, height] @board
              snake-positions (into #{} (:body @snake))

              current-point @point
              cells (for [y (range height)]
                      (into [:div.gamerow]
                            (for [x (range width)
                                  :let [current-pos [x y]]]
                              (cond
                                (snake-positions current-pos) (render-snake current-pos @snake)
                                (= current-pos current-point) (render-point)
                                :else (render-cell)))))]

          (into [:div.stage]
                cells))))))



(defn render-score
  "Render the player's score"
  []
  (let [points (subscribe [:points])]
    (fn []
      [:div.score (str "Score: " @points)])))


(defn render-state
  "Render the status of the game"
  []
  (let [game-state (subscribe [:game-state])]
    [:div.status (str "Game: " @game-state)]))


(defn render-pause
  "Render the paused/not paused state of the game"
  []

  (fn []
    (let [game-state (subscribe [:game-state])]
      (if (= @game-state :state-paused)
        [:div
         [:div.play
          [:h2 "Pause.."]]]
        [:div]))))


(defn render-loading
  "render the loading page"
  []
  (let [game-state (subscribe [:game-state])]
    (fn []
      (if (= @game-state :state-loaded)
        [:div
         [:div.play
          [:h2 "Welcome, boy..."]
          [:a {:on-click #(dispatch [:change-game-state :state-running])} "Click me to start"]]]
        [:div]))))


(defn render-game-over
  "Render the game"
  []
  (fn []
    (let [game-state (subscribe [:game-state])
          finished? (= :state-finished @game-state)]
      (if finished?
        [:div.overlay
         [:div.play {:on-click #(dispatch [:initialize])}
          [:h1 "↺"]]]
        [:div]))))

