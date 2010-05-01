(ns clj-swing.frame
  (:import (javax.swing JFrame ImageIcon))
  (:require [clojure.contrib.java-utils :as java]))


(defmacro set-constraint! [constraints field value]
  `(set! (. ~constraints ~(symbol (name field)))
         ~(if (keyword? value)
	    `(java/wall-hack-field  (class ~constraints) '~(symbol (name value))  (class ~constraints))
            value)))

(def *frame-on-close-actions*
  {:do-nothing (JFrame/DO_NOTHING_ON_CLOSE)
   :exit (JFrame/EXIT_ON_CLOSE)
   :hide (JFrame/HIDE_ON_CLOSE)
   :dispose (JFrame/DISPOSE_ON_CLOSE)})

(defmacro frame [& args]
  "options are:
:name - internal name of the frame.
:icon - icon, will be passed to javax.swing.ImageIcon.
:title - title for the frame.
:layout - layout manager.
:constrains - constrains object for the layout manager.
:decorated - sets weather or not the frame shall be undecorated. Default: true
:on-close - one of :do-nothing, :exit, :hide, :dispose, 
  sets the default on close action for the frame.
:look-and-feel-decorated - shall the window gets it's decorations from the look and feel.

:size - [w, h]
:bounds - [x, y, w, h]
:location - [x y]

:pack - shall the frame autopack at the end?
:show - shall the frame autoshow at the end?
"
    (let [default-opts 
	  {}
	  {forms :forms opts :options bindings :bindings} 
	  (reduce 
	   (fn [{options :options kw :kw state :state :as r} arg]
	     (cond
	      (= state :forms)
	      (update-in r [:forms] conj arg)
	      kw
	      (assoc r :options (assoc options kw arg) :kw nil)
	      (keyword? arg)
	      (assoc r :kw arg)
	      (vector? arg)
	      (assoc r :bindings arg :state :forms)))
	   {:options {} :kw nil :state :options :forms []} args)
	  opts (merge default-opts opts)
	  frame (or (:name opts) (gensym "frame"))
	  constrains (gensym "constrains")
	  manager (gensym "manager")]
      (prn opts)
      `(let [~frame  ~(if (:title opts)
			`(JFrame. ~(:title opts))
			`(JFrame.))
	     ~@(if (:layout opts)
		 ['_ `(.setLayout ~frame ~(:layout opts))])
	     ~@(if (:constrains opts)
		 `(~constrains ~(:constrains opts)))
	     ~@(if (:constrains opts)
		 (reverse 
		  (reduce
		   (fn [l [f s]]
		     (if (keyword? f)
		       (conj (conj l '_) `(set-constraint! ~constrains ~f ~s))
		       (conj (conj (conj (conj l f) s) '_) `(.add ~frame ~f ~constrains))
		       ))
		   '() (partition 2 bindings)))
		 (reverse 
		  (reduce
		   (fn [l [f s]]
		     (conj (conj (conj (conj l f) s) '_) `(.add ~frame ~f)))
		   '() (partition 2 bindings))))]
	 (doto ~frame
	   ~@(when-let [on-close (*frame-on-close-actions* (:on-close opts))]
	      [`(.setDefaultCloseOperation ~on-close)])
	   ~@(when-let [icon (:icon opts)]
	      [`(.setIconImage (.getImage (ImageIcon. icon)))])
	   ~@(when-let [decorated (:decorated opts)]
	      [`(.setUndecorated (not decorated))])
	   ~@(when-let [dlfd (:look-and-feel-decorated opts)]
	      [`(.setDefaultLookAndFeelDecorated dlfd)])
	   ~@(when-let [[w h] (:size opts)]
	      [`(.setSize w h)])
	   ~@(when-let [[x y w h] (:bounds opts)]
	      [`(.setBounds x y w h)])
	   ~@(when-let [[x y] (:location opts)]
	      [`(.setLocation x y)])
	   ~@(if (contains? opts :centered)
	      [`(.setLocationRelativeTo (:centered opts))])

	   ~@forms

	   ~@(if (:pack opts)
	      [`(.pack)])
	   ~@(if (:show opts)
	      [`(.setVisible true)])))))