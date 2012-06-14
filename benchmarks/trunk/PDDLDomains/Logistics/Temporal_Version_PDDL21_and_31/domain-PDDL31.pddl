;; Logistics domain, PDDL 3.1 version.

(define (domain logistics-object-fluents)

(:requirements :typing :equality :durative-actions :object-fluents) 

(:types  truck airplane - vehicle
         package vehicle - thing
         airport - location
         city location thing - object)

(:constant moving - location)
  
(:functions
	(city-of ?l - location) - city
        (location-of ?t - thing) - (either location vehicle))


(:durative-action drive
         :parameters    (?t - truck ?to - location)
         :duration (= ?duration 4)
         :condition  (and (over all (= (city-of (location-of ?t)) (city-of ?to)))
			  (at start (not (= (location-of ?t) ?to)))
         :effect    (and (at start (assign (location-of ?t) moving))
			(at end (assign (location-of ?t) ?to))

(:durative-action fly
         :parameters    (?a - airplane ?to - airport)
         :duration (= ?duration 8)
	 :condition (at start (not (= (location-of ?a) ?to)))
         :effect     (and (at start (assign (location-of ?a) moving))
			(at end (assign (location-of ?a) ?to)))

(:durative-action load
         :parameters    (?p - package ?v - vehicle ?l - location)
         :duration (= ?duration 2)
         :condition  (over all (location-of ?v) ?l)
         :effect     (over all (change (location-of ?p) ?l ?v))

(:durative-action unload
         :parameters    (?p - package ?v - vehicle ?l - location)
         :duration (= ?duration 2)
         :condition (over all (location-of ?v) ?l)
         :effect     (over all (change (location-of ?p) ?v ?l))
)

;; EOF
