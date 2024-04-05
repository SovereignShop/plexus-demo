(ns plexus-demo.core
   (:require 
    [clj-manifold3d.core :as m]
    [plexus.core 
     :refer [forward hull left right up down translate trim-by-plane
             set set-meta lookup-transform transform branch offset 
             rotate frame save-transform add-ns extrude to points export
             result difference union insert intersection loft export-models]]))
 
;; First, if you're new to Clojure and/or Calva, go through the Getting Started Tutorial for Calva
;; by issuing the command "Calva: Fire Up the Getting Started REPL". The examples will get you
;; up to speed with using a REPL.
;;
;; 
;; 
;; Now, after you've launched a repl and evaluated the ns form above,
;; let's create your first Manifold by evaluating this form:
 
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
;; You open this file with the command "Preferences: Open Keyboard Shortcuts (JSON)".
;; For example:
;;    {
;;        "key": "ctrl+alt+m",
;;        "command": "calva.runCustomREPLCommand",
;;        "args": {
;;            "snippet": "(require 'plexus.core)(plexus.core/export $current-form \"model.glb\")"
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

;; Similar to extrude, there is also revolve:

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
;;
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

;; With these functions, you can create just about anything you can imagine.
;;
;; Now that you understand Manifold, you're ready to understand how plexus
;; complements manifold and makes some models much easier to specify. Plexus
;; is essentialyl a DSL for extrude 3D gemotries. It enables you to "grow"
;; an arbitrarily complex tree of extrusions, which you then composite with CSG
;; operations.
;;
;; It's easiest to understand how it works with a simple example:

(extrude
 (result :name :pipes
         :expr (difference :body :mask))

 (frame :cross-section (m/circle 6) :name :body)
 (frame :cross-section (m/circle 4) :name :mask)
 (set :curve-radius 20 :to [:body]) (set :curve-radius 20 :to [:mask])

 (left :angle (/ Math/PI 2) :to [:body])
 (left :angle (/ Math/PI 2) :to [:mask])

 (right :angle (/ Math/PI 2) :to [:body])
 (right :angle (/ Math/PI 2) :to [:mask])

 (forward :length 10 :to [:body])
 (forward :length 10 :to [:mask])

 (up :angle (/ Math/PI 2) :to [:body])
 (up :angle (/ Math/PI 2) :to [:mask]))

;; Here, outer cross section is a circle with radius of 6. The mask
;; cross section is a circle of radius 4. We then specify a series of egocentric transformations to the
;; outer and inner cross sections.
;;
;; Obviously there is a lot of code duplication here. After providing the cross section for the inner and outer forms,
;; the transformations we apply to each are equivalent. We can get rid of that duplication by only providing one 
;; transforming both cross sections with each segment:

(extrude
 (result :name :pipes
         :expr (difference :body :mask))

 (frame :cross-section (m/circle 6) :name :body)
 (frame :cross-section (m/circle 4) :name :mask)
 (set :curve-radius 20 :to [:body :mask])

 (left :angle (/ Math/PI 2) :to [:body :mask])
 (right :angle (/ Math/PI 2) :to [:body :mask])
 (forward :length 10 :to [:body :mask])
 (up :angle (/ Math/PI 2) :to [:body :mask]))

;; As you can see, this is equivalent to the definition above.
;; But there still a lot of duplication. The `:to [:outer :inner]` is repeated in each segment.
;; We can elide this, as by default each segment will reply to every frame you have defined:

(extrude
 (result :name :pipes
         :expr (difference :body :mask))

 (frame :cross-section (m/circle 6) :name :body)
 (frame :cross-section (m/circle 4) :name :mask)
 (set :curve-radius 20)

 (left :angle (/ Math/PI 2))
 (right :angle (/ Math/PI 2))
 (forward :length 10)
 (up :angle (/ Math/PI 2)))

;; Now hopefully you understand the basic idea of plexus. You define a sequence of segments 
;; where each segment picks up where the previous one left off. You might think 
;; this is nice to have for rare cases, but isn't that general. Hopefully as we go through
;; more examples, you're start to see how general plexus can be. 
;;
;; Let's take a look now at how hulls are handled in plexus:

(extrude
 (result :name :pipes
         :expr (difference :body :mask))

 (frame :cross-section (m/circle 6) :name :body)
 (frame :cross-section (m/circle 4) :name :mask)
 (set :curve-radius 20)
 (hull
  (hull
   (forward :length 20)
   (set :cross-section (m/square 20 20 true) :to [:body])
   (set :cross-section (m/square 16 16 true) :to [:mask])
   (forward :length 20))
  (set :cross-section (m/circle 6) :to [:body])
  (set :cross-section (m/circle 4) :to [:mask])
  (forward :length 20)))

;; And, not surprisingly, loft is also available:

(extrude
 (result :name :pipes :expr :body)
 (frame :cross-section (m/difference (m/circle 20) (m/circle 18)) :name :body)
 (loft
  (forward :length 1)
  (for [_ (range 3)]
    [(translate :x 8)
     (forward :length 20)
     (translate :x -8)
     (forward :length 20)])))

;; Okay, that's somewhat handy. But with `branch` you really
;; start to see the power of plexus:

(def pi|2 (/ Math/PI 2))

(extrude
 (result :name :pipes
         :expr (difference :body :mask))

 (frame :cross-section (m/circle 6) :name :body)
 (frame :cross-section (m/circle 4) :name :mask)
 (set :curve-radius 10)

 (branch :from :body (left :angle pi|2) (right :angle pi|2) (forward :length 20))
 (branch :from :body (right :angle pi|2) (left :angle pi|2) (forward :length 20)))

;; The body of the branch form is the same as extrude, so you can nest arbitrarily.
;; The required ":from" property determines the starting coordinate frame of the branch. 
;; There's also an optional `:with` parameter that specifies which frames to include in the branch

(extrude
 (result :name :pipes
         :expr (difference :body :mask))

 (frame :cross-section (m/circle 6) :name :body)
 (frame :cross-section (m/circle 4) :name :mask)
 (set :curve-radius 10)

 (branch :from :body (left :angle pi|2) (right :angle pi|2) (forward :length 20))
 (branch
  :from :body
  :with [:body]
  (right :angle pi|2)
  (left :angle pi|2)
  (forward :length 20)))

;; You an also make any segment a gap using the `:gap` parameter:

(extrude
 (frame :cross-section (m/circle 6) :name :body :curve-radius 10)
 (for [_ (range 3)]
   [(left :angle (/ Math/PI 2) :gap true)
    (right :angle (/ Math/PI 2))]))

;; You can also specify which subset of active frames should be a gap by supplying a vector frame names:

(extrude
 (frame :cross-section (m/circle 6) :name :body :curve-radius 10)
 (for [_ (range 3)]
   [(left :angle (/ Math/PI 2) :gap [:body])
    (right :angle (/ Math/PI 2))]))

;; This is equivalent to above.
;;
;; Another powerful operation in extrusions is insert. Insert lets you
;; compose extrusions.

(let [pipe (extrude
            (result :name :pipe :expr (difference :outer :inner))
            (frame :cross-section (m/circle 6) :name :outer :curve-radius 10)
            (frame :cross-section (m/circle 4) :name :inner)
            (forward :length 30)) ]
  (extrude
   (result :name :pipes
           :expr (->> :pipe
                      (trim-by-plane {:normal [-1 0 0]})
                      (translate {:z 30})))
   (frame :name :origin)

   (for [i (range 4)]
     (branch
      :from :origin
      (rotate :x (* i 1/2 Math/PI))
      (insert :extrusion pipe)))))

;; We can also insert specific models with insert:

(let [pipe (extrude
            (result :name :pipe :expr (difference :outer :inner))
            (frame :cross-section (m/circle 6) :name :outer :curve-radius 10)
            (frame :cross-section (m/circle 4) :name :inner)
            (forward :length 30))]
  (extrude
   (result :name :pipes
           :expr (->> (difference :outer :inner)
                      (trim-by-plane {:normal [-1 0 0]})
                      (translate {:z 30})))
   (frame :name :origin)

   (for [i (range 4)]
     (branch
      :from :origin
      (rotate :x (* i 1/2 Math/PI))
      (insert :extrusion pipe
              :models [:outer :inner])))))

;; Notice the subtle difference in the result! Order of operations is an important
;; consideration when doing programmatic solid modelling. Often you'll find you want to defer
;; CSG compositing. Plexus purposely keeps the extrusion tree independent from the CSG tree.
;; Often you want to first define *all* of your extrusions, then do *all* of your compositing to form 
;; the final model. Historically, this was often ill-adviced because CSG operations were painfully
;; slow on large models. With Manifold, this is often no longer a concern.
;;
;; To fascitate creating large composites and overal good hygene, insert also supports namespacing of inserted models:

(let [pipe (extrude
            (result :name :pipe :expr (difference :outer :inner))
            (frame :cross-section (m/circle 6) :name :outer :curve-radius 10)
            (frame :cross-section (m/circle 4) :name :inner)
            (forward :length 30))]
  (extrude
   (result :name :pipes
           :expr (->> (difference :pipe/outer :pipe/inner)
                      (trim-by-plane {:normal [-1 0 0]})
                      (translate {:z 30})))
   (frame :name :origin)

   (for [i (range 4)]
     (branch
      :from :origin
      (rotate :x (* i 1/2 Math/PI))
      (insert :extrusion pipe
              :models [:outer :inner]
              :ns :pipe)))))
