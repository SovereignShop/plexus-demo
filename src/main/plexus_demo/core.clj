(ns plexus-demo.core
   (:require 
    [clj-manifold3d.core :as m]
    [plexus.core 
     :refer [forward hull left right up down backward translate
             set set-meta segment lookup-transform transform branch offset 
             rotate frame save-transform add-ns extrude to points export
             result difference union insert intersection loft export-models]]))
 
;; First, if you're new to Clojure and/or Calva, go through the Getting Started Tutorial for Calva
;; by issuing the command "Calva: Fire Up the Getting Started REPL". The examples will get you
;; up to speed with using a REPL.
;;
;; 
;; 
;; Now, let's create your first Manifold by evaluating this form:
 
(export (m/cube 40 40 60 true) "model.glb") ; Alt-Enter here to evaluate

;; You should see a `model.glb` file created in the root of the project directory. 
;; Go ahead and open it and drag it to a separate window.
;; You should see the rendered cube.
;;
;; Now let's change it slightly 

(export (m/cube 40 40 20 true) "model.glb")

;; You should see the render automatically update. This is the core of modelling with clj-manifold3d.
;; However, it would be nice to not have to wrap models in an export form all the time to view 
;; them.

(m/cube 40 40 40 true) ; Enter "ctrl-alt-space space", then select "d"

;; Now is is handy but also a bit cumbersome. I recommend setting up a key binding in keybindings.json.
;; For example:
;;    {
;;        "key": "ctrl+alt+m",
;;        "command": "calva.runCustomREPLCommand",
;;        "args": {
;;            "snippet": "(require 'plexus-demo.repl)(plexus-demo.repl/export-model $current-form)"
;;        }
;;    }
;;
;; 
;; You can also visualize manifold's 2D CrossSections the same way you visualize 3D Manifolds:
;; For example:

(m/square 50 50 true)

;; You should see a thin square. Of course, a cross-section does not actually have height, but 
;; when visualizing, cross sections are extruded to a height of 1/2 using another fundamental function
;; called `extrude`:

(m/extrude (m/square 50 50 true) 1/2)

;; Any CrossSection can be extruded into a Manifold representing a 3D object. 

;; Similar to extrude, there is also revolve

(m/revolve (m/square 10 10 false) 100 90)

;; Revolve forms a Manifold by revolving the cross-section around the Y-axis, then setting the Y-axis to the Z-axis.
;;
;; Next, let's demonstrate the core operations of manifold: union, difference, and intersectin.
;; These functions give you boolean operations on both cross-sections and manifolds.

(m/difference (m/square 40 40 true) (m/circle 25))

(m/union (m/square 40 40 true) (m/circle 25))

(m/intersection (m/square 40 40 true) (m/circle 25))

;; Of course, these also work on 3D objects:

(m/difference (m/cube 40 40 40 true) (m/sphere 25))

(m/union (m/cube 40 40 40 true) (m/sphere 25))

(m/intersection (m/cube 40 40 40 true) (m/sphere 25))

;; Next, we need a way to position objects in 3D space. We do this with
;; the operations translate, rotate, and (most generally) transform.

(m/difference (m/cube 40 40 40 true)
              (-> (m/sphere 25)
                  (m/translate [0 0 20])))

(m/difference (m/cube 40 40 40 true)
              (-> (m/sphere 25)
                  (m/translate [0 0 20])
                  (m/rotate [0 90 0])))

(m/difference (m/cube 40 40 40 true)
              (-> (m/sphere 25)
                  (m/transform (-> (m/frame 1)
                                   (m/rotate [0 (/ Math/PI 2) 0])
                                   (m/translate [0 0 20])))))

;; These are the fundamentals of solid modelling with manifold.
;; There are a few more high-level functions that are often quite useful.
;; The first is hull. Hull returns the Convex Hull of the input Manifolds 
;; or CrossSections that you give it.

(m/hull (m/cylinder 2 30) 
        (-> (m/sphere 5)
            (m/translate [0 0 30])))

;; As you can see, it looks a litle low-res. You can improve this by providing the
;; `circular-segments` parameter.

(m/hull (m/cylinder 2 30 30 100)
        (-> (m/sphere 5 100)
            (m/translate [0 0 30])))

;; In the case of the cylinder, there are 100 latitudinal segments.
;; In the cae of the sphere, we specify 100 latitudinal by 100 longitudinal segments.
;;
;; There is support for a primitive form of loft:

(m/loft (m/circle 10)
        [(m/frame 1)
         (-> (m/frame 1)
             (m/translate [0 5 15]))
         (-> (m/frame 1)
             (m/translate [0 0 30]))])

;; It simply "skins" over isomorphic cross-sections. You can also specify multiple cross-sections
;; as long as they all are composed of the same number of points:

(defn ovol
  ([rx ry]
   (ovol rx ry 30))
  ([rx ry n-steps]
   (for [x (range n-steps)]
     (let [d (* x (/ (* 2 Math/PI) n-steps))]
       [(* rx (Math/cos d))
        (* ry (Math/sin d))]))))

(m/loft (take 10
          (cycle
           [(ovol 25 18 70)
            (ovol 18 25 70)]))
        (for [i (range 10)]
          (m/translate (m/frame 1) [0 0 (* i 20)])))

;; Notice it tan take either CrossSections as input or sequences of points. Sequences are often prefered 
;; as CrossSections will remove coplanar points internally, resulting in unpredictable behavior.
;; 
;; You can also specify poyhedrons. Here is a cube defined as a polyhedron:


(m/polyhedron [[0 0 0]
               [5 0 0]
               [5 5 0]
               [0 5 0]
               [0 0 5]
               [5 0 5]
               [5 5 5]
               [0 5 5]]
              [[0 3 2 1]
               [4 5 6 7]
               [0 1 5 4]
               [1 2 6 5]
               [2 3 7 6]
               [3 0 4 7]])

;; This is a very general and powerful function in the hands of a skillful user.
;;
;; 