;; Logistics domain, PDDL 3.1 version.

(define (domain logistics-object-fluents)

(:requirements :typing :equality :object-fluents)

(:types package place city - object
        vehicle location - place
        airport - location
        truck airplane - vehicle)

(:functions (city-of ?l - location) - city
            (vehicle-location ?v - vehicle) - location
            (package-location ?p - package) - place)

(:action drive
         :parameters    (?t - truck ?l - location)
         :precondition  (= (city-of (vehicle-location ?t)) (city-of ?l))
         :effect        (assign (vehicle-location ?t) ?l))

(:action fly
         :parameters    (?a - airplane ?l - airport)
         :effect        (assign (vehicle-location ?a) ?l))

(:action load
         :parameters    (?p - package ?v - vehicle)
         :precondition  (= (package-location ?p) (vehicle-location ?v))
         :effect        (assign (package-location ?p) ?v))

(:action unload
         :parameters    (?p - package ?v - vehicle)
         :precondition  (= (package-location ?p) ?v)
         :effect        (assign (package-location ?p) (vehicle-location ?v)))

)
