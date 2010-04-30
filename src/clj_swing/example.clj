(ns clj-swing.example
  (:use [clj-swing core label]))


(defn draw-sort [c alg]
  (set-text! c alg))

(defn grid-sort-app []
  (frame :title "Sort Visualizer" :layout (GridLayout. 2 2 2 2) 
	      [algorithm-chooser (combo-box ["Quick sort" "Bubble Sort"])
	       canvas (label "")
	       l1 (label "Algorithms")
	       l2 (label "Button")
	       button (button b "Run Algorithm"
			      ([event] 
				 (draw-sort canvas (selected-item algorithm-chooser))))]
	      (.setSize 250 250)
	      (.setVisible true)))

(defn grid-bag-sort-app []
  (frame "Sort Visualizer" (GridBagLayout.) (java.awt.GridBagConstraints.)
		  [:gridx 0 :gridy 0 :anchor :LINE_END
		   _ (label "Algorithms")
		   :gridy 1
		   _ (label "Button")
		   :gridy 2
		   canvas (label "")
		   :gridx 1 :gridy 0 :anchor :LINE_START
		   algorithm-chooser (combo-box ["Quick sort" "Bubble Sort"])
		   :gridy 1
		   _ (button b "Run Algorithm" 
				  ([event]
				     (draw-sort canvas (selected-item algorithm-chooser))))]
		  (.setSize 300 300)
		  (.setVisible true)))


(frame :title "Test Frame" :layout (java.awt.GridBagLayout.) :constrains (java.awt.GridBagConstraints.) [:gridx 0 :gridy 0 _ (label "0/0") :gridx 1 :gridy 1 (label "1/1")] (.setVisible true))

(frame :title "Test Frame" :layout (java.awt.GridBagLayout.) :constrains (java.awt.GridBagConstraints.) [:gridx 0 :gridy 0 _ (label "0/0")] (.setVisible true))