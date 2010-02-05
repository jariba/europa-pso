#ifndef _EDGE_ITERATOR_HEADER_
#define _EDGE_ITERATOR_HEADER_

/**
 * @file EdgeIterator.hh
 * @author David Rijsman
 * @brief Defines the public interface for an edge iterator
 * @date April 2006
 * @ingroup Resource
 */

#include "Types.hh"
#include "Node.hh"

namespace EUROPA
{
    /**
     * @brief
     */
    class EdgeIterator
    {
    public:
      /**
       * @brief
       */
      inline bool ok() const;
      /**
       * @brief
       */
      inline Edge* operator*() const;
    protected:
      /**
       * @brief
       * @par edges
       * @par mustBeEnabled If true iterates over enabled edges otherwise iterates over all edges
       */
      EdgeIterator( const EdgeList& edges, bool mustBeEnabled ):
	m_Iterator( edges.begin() ),
	m_End( edges.end() ),
	m_Enabled( mustBeEnabled )
      {
      }
    protected:
      EdgeList::const_iterator m_Iterator;
      EdgeList::const_iterator m_End;
      bool m_Enabled;
    };

    bool EdgeIterator::ok() const
    {
      return m_Iterator != m_End;
    }


    Edge* EdgeIterator::operator*() const
    {
      if( ok() )
	return (*m_Iterator);

      return 0;
    }

    /**
     * @brief
     */
    class EdgeOutIterator:
      public EdgeIterator
    {
    public:
      /**
       * @brief
       * @par node
       * @par mustBeEnabled If true iterates over enabled edges otherwise iterates over all edges
       */
      EdgeOutIterator( const Node& node, bool mustBeEnabled = true ):
	EdgeIterator( node.getOutEdges(), mustBeEnabled )
      {
	if( m_Enabled )
	  {
	    while( m_Iterator != m_End && ( !(*m_Iterator)->isEnabled() || !(*m_Iterator)->getTarget()->isEnabled() ) )
	      ++m_Iterator;
	  }
      }

      void operator++()
      {
	++m_Iterator;

	if( m_Enabled )
	  {
	    while( m_Iterator != m_End && ( !(*m_Iterator)->isEnabled() || !(*m_Iterator)->getTarget()->isEnabled() ) )
	      ++m_Iterator;
	  }
      }

    };

    /**
     * @brief
     */
    class EdgeInIterator:
      public EdgeIterator
    {
    public:
      /**
       * @brief
       * @par node
       * @par mustBeEnabled If true iterates over enabled edges otherwise iterates over all edges
       */
      EdgeInIterator( const Node& node, bool mustBeEnabled = true  ):
	EdgeIterator( node.getInEdges(), mustBeEnabled )
      {
	if( m_Enabled )
	  {
	    while( m_Iterator != m_End && ( !(*m_Iterator)->isEnabled() || !(*m_Iterator)->getSource()->isEnabled() ) )
	      ++m_Iterator;
	  }
      }

      void operator++()
      {
	++m_Iterator;

	if( m_Enabled )
	  {
	    while( m_Iterator != m_End && ( !(*m_Iterator)->isEnabled() || !(*m_Iterator)->getSource()->isEnabled() ) )
	      ++m_Iterator;
	  }
      }

    };
}

#endif // EDGE_ITERATOR_HEADER_

