#include "LabelStr.hh"
#include "LockManager.hh"
#include "Error.hh"
#include "Utils.hh"
#include <string.h>

namespace EUROPA {

  DEFINE_GLOBAL_CONST(LabelStr, EMPTY_LABEL, "");

  pthread_mutex_t LabelStr::s_mutex = PTHREAD_MUTEX_INITIALIZER;

//   std::map< std::string, double>& LabelStr::keysFromString() {
//     static std::map< std::string, double> sl_keysFromString;
//     return(sl_keysFromString);
//   }

//   std::map< double, std::string>& LabelStr::stringFromKeys() {
//     static std::map< double, std::string> sl_stringFromKeys;
//     return(sl_stringFromKeys);
//   }

  __gnu_cxx::hash_map<std::string, double>& LabelStr::keysFromString() {
    static __gnu_cxx::hash_map<std::string, double> sl_keysFromString;
    return(sl_keysFromString);
  }

  __gnu_cxx::hash_map< double, std::string>& LabelStr::stringFromKeys() {
    static __gnu_cxx::hash_map<double, std::string> sl_stringFromKeys;
    return(sl_stringFromKeys);
  }

  LabelStr::LabelStr() {
    std::string empty("");
    m_key = getKey(empty);
#ifndef EUROPA_FAST
    m_chars = empty.c_str();
#endif
  }

  /**
   * Construction must obtain a key that is efficient to use for later
   * calculations in the domain and must maintain the ordering defined
   * by the strings.
   */
  LabelStr::LabelStr(const std::string& label) {
    m_key = getKey(label);

#ifndef EUROPA_FAST
    m_chars = label.c_str();
#endif
  }

  LabelStr::LabelStr(const char* label) {
    std::string str(label);
    m_key = getKey(label);

#ifndef EUROPA_FAST
    m_chars = label;
#endif
  }

  LabelStr::LabelStr(double key)
    : m_key(key) {
    check_error(isString(m_key), "Invalid key provided.");

#ifndef EUROPA_FAST
    m_chars = toString().c_str();
#endif
  }

  const std::string& LabelStr::toString() const {
    return(getString(m_key));
  }

  const char* LabelStr::c_str() const {
    return(toString().c_str());
  }

#ifndef EUROPA_FAST

  LabelStr::LabelStr(const LabelStr& org)
    : m_key(org.m_key) {
    m_chars = org.m_chars;
  }

  LabelStr::operator double () const {
    return(m_key);
  }

#endif

  bool LabelStr::operator <(const LabelStr& lbl) const {
    return toString() < lbl.toString();
  }

  bool LabelStr::operator >(const LabelStr& lbl) const {
    return toString() > lbl.toString();
  }

  unsigned int LabelStr::getSize() {
    check_error(keysFromString().size() == stringFromKeys().size());
    return(keysFromString().size());
  }

  double LabelStr::getKey(const std::string& label) {
    static double sl_counter = EPSILON;

    __gnu_cxx::hash_map<std::string, double>::iterator it = 
      keysFromString().find(label);

    if (it != keysFromString().end())
      return(it->second); // Found it; return the key.

    // Given label not found, so allocate it.
    double key = sl_counter;
    sl_counter = sl_counter + 2*EPSILON;

    check_error(key < 1.0, "More strings allocated than permitted");
    
    handleInsertion(key, label);
    return(key);
  }

  void LabelStr::handleInsertion(double key, const std::string& label) {
    keysFromString().insert(std::pair< std::string, double >(label, key));
    stringFromKeys().insert(std::pair< double, std::string >(key, label));
  }

  const std::string& LabelStr::getString(double key){
    __gnu_cxx::hash_map< double, std::string >::const_iterator it = stringFromKeys().find(key);
    check_error(it != stringFromKeys().end());
    return(it->second);
  }

  bool LabelStr::isString(double key) {
    return(stringFromKeys().find(key) != stringFromKeys().end());
  }

  bool LabelStr::isString(const std::string& candidate){
    return (keysFromString().find(candidate) != keysFromString().end());
  }

  bool LabelStr::contains(const LabelStr& lblStr) const{
    const std::string& thisStr = toString();
    int index = thisStr.find(lblStr.c_str());
    return (index >= 0);
  }


  unsigned int LabelStr::countElements(const char* delimiter) const{
    check_error(delimiter != NULL && delimiter != 0 && delimiter[0] != '\0', "'NULL' and '\\0' are not valid delimiters");

    //allocate a results vector
    std::vector<std::string> tokens;

    // Get a std string from the LabelStr
    const std::string& srcStr = toString();

    //create a std string of the delimiter
    std::string delim(delimiter);

    tokenize(srcStr, tokens, delim);

    return tokens.size();
  }

  LabelStr LabelStr::getElement(unsigned int index, const char* delimiter) const{
    check_error(delimiter != NULL && delimiter != 0 && delimiter[0] != '\0', "'NULL' and '\\0' are not valid delimiters");

    //allocate a results vector
    std::vector<std::string> tokens;

    // Get a std string from the LabelStr
    const std::string& srcStr = toString();

    //create a std string of the delimiter
    std::string delim(delimiter);

    tokenize(srcStr, tokens, delim);

    LabelStr result(tokens[index]);

    return result;
  }
}
