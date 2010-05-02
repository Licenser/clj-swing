(ns clj-swing.example
  (:use [clj-swing frame label button combo-box list panel]))

(import '(javax.swing  UIManager)
	'(java.awt BasicStroke Color Dimension Graphics Graphics2D RenderingHints)
	'(java.awt.geom AffineTransform Ellipse2D)
	'(java.awt GridBagLayout GridLayout GridBagConstraints))

(def sr (ref '["Quick sort" "Bubble Sort"]))
(def lm (ref '["Bla" "Blubb"]))
(def selected (atom nil))
(def nativeLF (. UIManager getSystemLookAndFeelClassName))

(. UIManager setLookAndFeel nativeLF)

(defn paint-donut [g]
  (println "y!!!o")
  (let [width 360
	height 310
	ellipse (new java.awt.geom.Ellipse2D$Double 0 0 80 130)
	at (AffineTransform/getTranslateInstance (/ width 2) (/ height 2))]
    (doto g
      (.setStroke (BasicStroke. 1))
      (.setColor (. Color gray)))
    (doseq [i (range 0 361 5)]
      (.rotate g (Math/toRadians i))
      (.draw g (.createTransformedShape at ellipse)))))

(defn graphics-example []
  (frame
   :title "Graphics example" 
   :show true :pack true
   [p (panel
       :preferred-size [360 310]
       :focusable true
       :paint ([g]
		 (proxy-super paintComponent g)
		 (paint-donut g)))]))

(defn grid-bag-example []
  (frame :title "Sort Visualizer" :layout (GridBagLayout.) :constrains (java.awt.GridBagConstraints.) :name fr
	 :show true :pack true
		  [:gridx 0 :gridy 0 :anchor :LINE_END
		   _ (label "Algorithms")
		   :gridy 1
		   _ (label "Button")
		   :gridx 1 :gridy 0 :anchor :LINE_START
		   _ (combo-box [] :model (seq-ref-combobox-model sr selected))
		   :gridy 1
		   _ (button "Run Algorithm" 
			     :action ([_] (if @selected (dosync (alter lm conj @selected)))))
		   :gridx 3 :gridy 0 :gridheight 2 :anchor :CENTER
		   _ (scroll-panel (jlist :model (seq-ref-list-model lm)) :preferred-size [150 100])]))