(ns snake-game.logic)

(defn random-free-position
  "This function takes the snake and the board-size as arguments,
  and returns a random position not colliding with the snake body"
  [snake [x y]]
  (let [snake-postions-set (into #{} (:body snake))
        board-positions (for [x-pos (range x)
                              y-pos (range y)]
                          [x-pos y-pos])]
    (when-let [free-positions (seq (remove snake-postions-set board-positions))]
      (rand-nth free-positions))))
