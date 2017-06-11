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


;; waarom update-in ipv. update?
;; waarom niet assoc of assoc-in
(defn move-snake
  "Move the whole snake based on the position and direction of each snake element"
  [{:keys [direction body] :as snake}]
  (let [head-new-position (mapv + direction (first body))]
    ;; (update snake :body #(into [] (drop-last (cons head-new-position body)))
    ;; (assoc snake :body (into [] (drop-last (cons head-new-position body)))
    (update-in snake [:body] #(into [] (drop-last (cons head-new-position body)))))
  )

(def key-code->move
  "Maps key-codes to moves"
  {37 [-1 0]                                                ;up
   38 [0 -1]                                                 ;right
   39 [1 0]                                                 ;down
   40 [0 1]                                                ;left
   })

(defn change-snake-direction
  "Return a valid new direction or the current one"
  [[new-x new-y] [x y]]
  (if (or
        (= new-x x)
        (= new-y y))
    [x y]
    [new-x new-y]))


(defn snake-tail [lst-but-one lst]
  "Compute x or y tail coordinate based on the last two values of that coordinate"
  (if (= lst-but-one lst)
    lst-but-one
    (if (> lst-but-one lst)
      (dec lst)
      (inc lst)
      ))
  )

(defn grow-snake
  "Append a new tail body segment to the snake"
  [{:keys [body direction] :as snake}]
  (let [
        [[first-x first-y] [second-x second-y]] (take-last 2 body)
        x (snake-tail first-x second-x)
        y (snake-tail first-y second-y)
        ]
    ;(update snake :body #(conj % [x y]))
    (update-in snake [:body] #(conj % [x y]))
    ))

(defn process-move
  "Evaluate a new snake posistion in the context of the whole game"
  [{:keys [snake point board] :as db}]
  (if (= point (first (:body snake)))
    (-> db
        (update-in [:snake] grow-snake)
        (update-in [:points] inc)
        (assoc :point (random-free-position snake board)))
    db))
