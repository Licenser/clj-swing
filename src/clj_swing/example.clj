(ns clj-swing.example
  (:use [clj-swing frame label button combo-box]))



(import '(javax.swing JFrame JLabel JTextField JButton JComboBox JPanel Timer)
	'(java.awt.event ActionListener)
	'(java.awt GridBagLayout GridLayout GridBagConstraints))

(defn draw-sort [c alg]
  (.setText c alg))

(def sr (ref '("Quick sort" "Bubble Sort")))

(defn grid-sort-app []
  (frame :title "Sort Visualizer" :layout (GridLayout. 2 2 2 2) 
	      [algorithm-chooser (combo-box [] :model (seq-ref-combobox-model sr))
	       output (label "")
	       l1 (label "Algorithms")
	       l2 (label "Button")
	       button (button "Run Algorithm"
			      :action ([event] 
				 (.setText output (selected-item algorithm-chooser))))]
	      (.setSize 250 250)
	      (.setVisible true))
)

(defn grid-bag-sort-app []
  (frame :title "Sort Visualizer" :layout (GridBagLayout.) :constrains (java.awt.GridBagConstraints.)
	 :show true :pack true
		  [:gridx 0 :gridy 0 :anchor :LINE_END
		   _ (label "Algorithms")
		   :gridy 1
		   _ (label "Button")
		   :gridy 2
		   canvas (label "")
		   :gridx 1 :gridy 0 :anchor :LINE_START
		   algorithm-chooser (combo-box [] :model (seq-ref-combobox-model sr))
		   :gridy 1
		   _ (button "Run Algorithm" 
			     :action ([event]
					(.setText canvas (selected-item algorithm-chooser))))])
)


(frame :title "Test Frame" :layout (java.awt.GridBagLayout.) :constrains (java.awt.GridBagConstraints.) [:gridx 0 :gridy 0 _ (label "0/0") :gridx 1 :gridy 1 _ (label "1/1")] (.setVisible true))

(frame :title "Test Frame" :layout (java.awt.GridBagLayout.) :constrains (java.awt.GridBagConstraints.) [:gridx 0 :gridy 0 _ (label "0/0")] (.setVisible true))