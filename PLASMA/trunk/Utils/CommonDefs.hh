#ifndef _H_CommonDefs
#define _H_CommonDefs

#include <cassert>

#ifdef __BEOS__
#include <debugger.h>
#endif

#include <sstream>

#include "Error.hh"

/**
 * @def assertTrue
 * @brief Require the condition to be true, aborting the program otherwise.
 * @note Should only be used in test programs.
 * @note Likely precursor to new error handling support.
 */
#define assertTrue(cond) (assert(cond))

/**
 * @def assertFalse
 * @brief Require the condition to be false, aborting the program otherwise.
 * @note Should only be used in test programs.
 * @note Likely precursor to new error handling support.
 */
#define assertFalse(cond) (assert(!(cond)))

#define streamIsEmpty(s) ((s).str() == "")

/**
 * @def DECLARE_GLOBAL_CONST(TYPE,NAME)
 * @brief Declare a global constant via a global function to ensure initialization
 * occurs before use with all linkers.
 */
#define DECLARE_GLOBAL_CONST(TYPE, NAME) \
  extern const TYPE& NAME();

/**
 * @def DEFINE_GLOBAL_CONST(TYPE,NAME,VALUE)
 * @brief Define a global constant to have the given value via a
 * global function to ensure initialization occurs before use with all
 * linkers.
 */
#define DEFINE_GLOBAL_CONST(TYPE, NAME, VALUE) \
  const TYPE& NAME() { \
    static const TYPE sl_data(VALUE); \
    return(sl_data); \
  }

/**
 * @def DEFINE_GLOBAL_EMPTY_CONST(TYPE,NAME)
 * @brief Define a global constant via a global function to ensure
 * initialization occurs before use with all linkers.
 */
#define DEFINE_GLOBAL_EMPTY_CONST(TYPE, NAME) \
  const TYPE& NAME() { \
    static const TYPE sl_data; \
    return(sl_data); \
  }

DECLARE_GLOBAL_CONST(bool, g_alwaysFails);
#define ALWAYS_FAILS (g_alwaysFails())

namespace PLASMA {
  DECLARE_GLOBAL_CONST(int, g_maxInt);
  DECLARE_GLOBAL_CONST(int, g_infiniteTime);
  DECLARE_GLOBAL_CONST(int, g_noTime);
  DECLARE_GLOBAL_CONST(double, g_epsilon);
}

#define MAX_INT (PLASMA::g_maxInt())

#define MAX_FINITE_TIME (MAX_INT)

#define MIN_FINITE_TIME (-MAX_INT)

#define PLUS_INFINITY (PLASMA::g_infiniteTime())

#define MINUS_INFINITY (-PLASMA::g_infiniteTime())

/**
 * @def EPSILON
 * Used when computing differences and comparing real numbers:
 * smallest recognized increment.
 */
#define EPSILON (PLASMA::g_epsilon())

#endif
