#ifndef _H_Instant
#define _H_Instant

/**
 * @file Instant.hh
 * @author Michael Iatauro
 * @brief Defines a class representing an instantaneous change in resource level.
 * @date March, 2006
 * @ingroup Resource
 */

#include "ResourceDefs.hh"
#include "Entity.hh"

#include <set>

namespace EUROPA {

    /**
     * @class Instant
     * @brief A class representing an instantaneous change in resource level.
     *
     * The Instant class represents a moment in time at which the level of a resource may change,
     * which are the only times of interest for profile calculation.  Each Instant maintains
     * a set of Transactions that overlap the time in some way.
     */
    class Instant : public Entity {
    public:

      DECLARE_ENTITY_TYPE(Instant);

      ~Instant();

      InstantId getId() const {return m_id;}
      /**
       * @brief Get the time of the Instant.
       * @return the time
       */
      eint getTime() const;

      /**
       * @brief Get the complete set of transactions that overlap this Instant.
       * @return A const ref to the set of transactions.
       */
      const std::set<TransactionId>& getTransactions() const;

      /**
       * @brief Get the set of transactions whose latest time is equal to this instant.
       * @return A const ref to the set of transactions.
       */
      const std::set<TransactionId>& getEndingTransactions() const;

      /**
       * @brief Get the set of transactions whose earliest time is equal to this instant.
       * @return A const ref to the set of transactions.
       */
      const std::set<TransactionId>& getStartingTransactions() const;

      /**
       * @brief Get the set of transactions whose time overlaps this instant.
       * @return A const ref to the set of transactions.
       */
      const std::set<TransactionId>& getOverlappingTransactions() const;

      /**
       * @brief Gets the lower level of the profile at this instant.  May cause profile recalculation.
       * @return the lower level of the profile
       */
      edouble getLowerLevel();

      /**
       * @brief Gets the upper bound of the lower level at this instant.  May cause profile recalculation.
       * @return The upper bound of the lower level of the profile
       */
      edouble getLowerLevelMax();

      /**
       * @brief Gets the upper level of the profile at this instant.  May cause profile recalculation.
       * @return the upper level of the profile
       */
      edouble getUpperLevel();

      /**
       * @brief Gets the lower bound of the upper level at this instant.  May cause profile recalculation.
       * @return The lower bound of the upper level of the profile
       */
      edouble getUpperLevelMin();

      /**
       * @brief Get the upper bound of the amount of production possible at this instant.  May cause profile recalculation.
       * @return the maximum amount of production at this instant.
       */
      edouble getMaxInstantProduction();

      /**
       * @brief Get the upper bound of the amount of consumption possible at this instant.  May cause profile recalculation.
       * @return the maximum amount of consumption at this instant.
       */
      edouble getMaxInstantConsumption();

      /**
       * @brief Get the lower bound of the amount of production possible at this instant.  May cause profile recalculation.
       * @return the maximum amount of production at this instant.
       */
      edouble getMinInstantProduction();

      /**
       * @brief Get the lower bound of the amount of consumption possible at this instant.  May cause profile recalculation.
       * @return the maximum amount of consumption at this instant.
       */
      edouble getMinInstantConsumption();

      /**
       * @brief Get the maximum amount of consumption that can have happened by this instant.
       * @return the maximum amount of consumption that has happened by now
       */
      edouble getMaxCumulativeConsumption();

      /**
       * @brief Get the maximum amount of production that can have happened by this instant.
       * @return the maximum amount of production that has happened by now
       */
      edouble getMaxCumulativeProduction();

      /**
       * @brief Get the minimum amount of consumption that can have happened by this instant.
       * @return the minimum amount of consumption that has happened by now
       */
      edouble getMinCumulativeConsumption();

      /**
       * @brief Get the minimum amount of production that can have happened by this instant.
       * @return the minimum amount of production that has happened by now
       */
      edouble getMinCumulativeProduction();

      edouble getMinPrevConsumption();
      edouble getMaxPrevConsumption();
      edouble getMinPrevProduction();
      edouble getMaxPrevProduction();


      /**
       * @brief Update the information about the profile at this instant.
       * @param lowerLevelMin The lowest level of the profile at this instant.
       * @param lowerLevelMax The upper bound of the lower level of the profile at this instant.
       * @param upperLevelMin The lower bound of the upper level of the profile at this instant.
       * @param upperLevelMax The highest level of the profile at this instant.
       * @param minInstConsumption The least amount of consumption at this instant
       * @param maxInstConsumption The greatest amount of consumption at this instant.
       * @param minInstProduction The least amount of production at this instant.
       * @param maxInstProduction The greatest amount of production at this instant.
       * @param minCumulativeConsumption The least amount of consumption that can have happened by this instant.
       * @param maxCumulativeConsumption The greatest amount of consumption that can have happened by this instant.
       * @param minCumulativeProduction The least amount of production that can have happened by this instant.
       * @param maxCumulativeProduction The greatest amount of production that can have happened by this instant.
       * @param minPrevConsumption
       * @param maxPrevConsumption
       * @param minPrevProduction
       * @param maxPrevProduction
       */
      void update(edouble lowerLevelMin, edouble lowerLevelMax, edouble upperLevelMin, edouble upperLevelMax,
		  edouble minInstConsumption, edouble maxInstConsumption, edouble minInstProduction, edouble maxInstProduction,
		  edouble minCumulativeConsumption, edouble maxCumulativeConsumption, edouble minCumulativeProduction, edouble maxCumulativeProduction,
		  edouble minPrevConsumption, edouble maxPrevConsumption, edouble minPrevProduction, edouble maxPrevProduction);

      /**
       * @brief Determines whether this instant contains a flaw.
       * @return True if there is a flaw, false otherwise.
       */
      bool isFlawed() const {return m_flawed;}

      /**
       * @brief Determines whether this instant contains a violation.
       * @return True if there is a violation, false otherwise.
       */
      bool isViolated() const {return m_violated;}

      /**
       * @brief Set the flaw status of this instant.
       * @param flawed True if there is a flaw, false otherwise.
       */
      void setFlawed(const bool flawed) {m_flawed = flawed;  if(!flawed) {m_upperFlaw = false; m_lowerFlaw = false; m_upperFlawMagnitude = 0; m_lowerFlawMagnitude = 0;}}

      void setUpper(const bool upperFlaw) {m_upperFlaw = upperFlaw;}

      void setLower(const bool lowerFlaw) {m_lowerFlaw = lowerFlaw;}

      void setUpperMagnitude(const edouble m) {check_error(m_upperFlaw); m_upperFlawMagnitude = m;}

      void setLowerMagnitude(const edouble m) {check_error(m_lowerFlaw); m_lowerFlawMagnitude = m;}

      bool hasUpperLevelFlaw() const {return m_upperFlaw;}

      bool hasLowerLevelFlaw() const {return m_lowerFlaw;}

      edouble getUpperFlawMagnitude() const {return m_upperFlawMagnitude;}

      edouble getLowerFlawMagnitude() const {return m_lowerFlawMagnitude;}

      /**
       * @brief Set the violation status of this instant.
       * @param violationed True if there is a violation, false otherwise.
       */
      void setViolated(const bool violated) {m_violated = violated;}

      std::string toString() const;

      const ProfileId& getProfile() const {return m_profile;}
    protected:
    private:
      friend class Profile;
      /**
       * @brief Constructor.  This is private because nothing outside of a Profile should instantiate an Instant.
       * @param time The time this Instant represents.
       * @param prof The profile this Instant is on.  Used to determine if queries should cause recalculation.
       */
      Instant(const eint time, const ProfileId prof);

      /**
       * @brief Adds a transaction to the set overlapping this time.
       * @param t The transaction.
       */
      void addTransaction(const TransactionId t);
      /**
       * @brief Updates a transaction already in the set overlapping this time.
       * @param t The transaction.
       */
      void updateTransaction(const TransactionId t);

      /**
       * @brief Removes a transaction from the set overlapping this time.
       * @param t The transaction.
       */
      void removeTransaction(const TransactionId t);

      /**
       * @brief Determines if this Instant is at the start or end of a transaction.  Used to decide if an Instant that has transactions should be deleted.
       * @return True if the time of the Instant is equal to one of the bounds of one of the times of the transactions overlapping this instant.
       */
      bool containsStartOrEnd();

      InstantId m_id;
      eint m_time; /*<! The time of the Instant */
      ProfileId m_profile; /*<! The Profile the Instant is on */
      edouble m_lowerLevel, m_lowerLevelMax, m_upperLevelMin, m_upperLevel; /*<! The four bounds of the level. */
      edouble m_maxInstProduction, m_maxInstConsumption, m_minInstProduction, m_minInstConsumption; /*<! The bounds around instantaneous consumption and production */
      edouble m_maxCumulativeProduction, m_maxCumulativeConsumption, m_minCumulativeProduction, m_minCumulativeConsumption; /*<! The bounds around cumulative consumption and production */
      edouble m_maxPrevProduction, m_maxPrevConsumption, m_minPrevProduction, m_minPrevConsumption; /*<! The bounds on consumption and production necessarily before this instant*/
      edouble m_upperFlawMagnitude, m_lowerFlawMagnitude; /*<! The magnitude of the differences between the levels and the limits */
      bool m_violated, m_flawed, m_upperFlaw, m_lowerFlaw; /*<! Flaw and violation flags */
      std::set<TransactionId> m_transactions; /*<! The complete set of transactions */
      std::set<TransactionId> m_endingTransactions; /*<! The set of transactions whose upper bound is equal to the current time. */
      std::set<TransactionId> m_startingTransactions; /*<! The set of transactions whose lower bound is equal to the current time. */
    };
}

#endif
