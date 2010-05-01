(ns clj-swing.example
  (:use [clj-swing frame label button combo-box list panel]))

(import '(javax.swing JFrame JLabel JTextField JButton JComboBox JPanel Timer)
	'(java.awt.event ActionListener)
	'(java.awt GridBagLayout GridLayout GridBagConstraints))

(def sr (ref '["Quick sort" "Bubble Sort"]))
(def lm (ref '["Bla" "Blubb"]))

(defn grid-bag-sort-app []
  (frame :title "Sort Visualizer" :layout (GridBagLayout.) :constrains (java.awt.GridBagConstraints.) :name fr
	 :show true :pack true
		  [:gridx 0 :gridy 0 :anchor :LINE_END
		   _ (label "Algorithms")
		   :gridy 1
		   _ (label "Button")
		   :gridx 1 :gridy 0 :anchor :LINE_START
		   algorithm-chooser (combo-box [] :model (seq-ref-combobox-model sr))
		   :gridy 1
		   _ (button "Run Algorithm" 
			     :action ([event]
					(dosync (alter lm conj (selected-item algorithm-chooser)))))
		   :gridx 3 :gridy 0 :gridheight 2 :anchor :CENTER
		   _ (scroll-panel (jlist :model (seq-ref-list-model lm)) :preferred-size [150 100])
])
)


(comment
(frame :title "Test Frame" :layout (java.awt.GridBagLayout.) :constrains (java.awt.GridBagConstraints.) [:gridx 0 :gridy 0 _ (label "0/0") :gridx 1 :gridy 1 _ (label "1/1")] (.setVisible true))

(frame :title "Test Frame" :layout (java.awt.GridBagLayout.) :constrains (java.awt.GridBagConstraints.) [:gridx 0 :gridy 0 _ (label "0/0")] (.setVisible true)))