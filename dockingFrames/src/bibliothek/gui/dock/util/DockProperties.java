/**
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2007 Benjamin Sigg
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Benjamin Sigg
 * benjamin_sigg@gmx.ch
 * CH - Switzerland
 */

package bibliothek.gui.dock.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A set of properties that are used at different places all over the framework.
 * No component should expect that there are any entries in this map.
 * @author Benjamin Sigg
 *
 */
public class DockProperties {
	/** the map of values */
	private Map<PropertyKey<?>, Entry<?>> map = new HashMap<PropertyKey<?>, Entry<?>>();
	
	/**
	 * Sets a value.
	 * @param <A> the type of the value
	 * @param key the key to access the value
	 * @param value the value, can be <code>null</code>
	 */
	public <A> void set( PropertyKey<A> key, A value ){
		Entry<A> entry = getEntry( key, true );
		entry.setValue( value );
		entry.setHasBeenSet( true );
		check( entry );
	}
	
	/**
	 * Either sets the property <code>key</code> to <code>value</code> or
	 * set the default value for <code>key</code>.
	 * @param <A> the type of the value
	 * @param key the key to access the value
	 * @param value the new value, if <code>null</code> then the default
	 * value will be set
	 */
	public <A> void setOrRemove( PropertyKey<A> key, A value ){
		if( value == null )
			toDefault( key );
		else
			set( key, value );
	}
	
	/**
	 * Tells the entry <code>key</code> that the user has never set its value.
	 * Also removes the old value of the entry.
	 * @param key the key to access the entry
	 */
	public void toDefault( PropertyKey<?> key ){
	    Entry<?> entry = getEntry( key, true );
        entry.setHasBeenSet( false );
        entry.setValue( null );
        check( entry );
	}
	
	/**
	 * Gets the value accessed by <code>key</code>. If the value in the
	 * properties is <code>null</code>, then the {@link PropertyKey#getDefault() default}
	 * value is returned.
	 * @param <A> the type of the value
	 * @param key the key to search
	 * @return the value or <code>null</code>
	 */
	public <A> A get( PropertyKey<A> key ){
		Entry<A> entry = getEntry( key, false );
		A result;
		if( entry == null )
			result = key.getDefault();
		else{
			result = entry.getValue();
		}
		
		return result;
	}
	
	/**
	 * Adds a listener that will be informed whenever the value accessed
	 * through <code>key</code> changes.
	 * @param <A> the type of the value
	 * @param key the key that accesses the value
	 * @param listener the new listener
	 */
	public <A> void addListener( PropertyKey<A> key, DockPropertyListener<A> listener ){
		if( listener == null )
			throw new IllegalArgumentException( "Listener must not be null" );
		getEntry( key, true ).addListener( listener );
	}
	
	/**
	 * Removes an earlier added listener.
	 * @param <A> the type of value observed by the listener
	 * @param key the key to access the observed entry
	 * @param listener the listener to remove
	 */
	public <A> void removeListener( PropertyKey<A> key, DockPropertyListener<A> listener ){
		Entry<A> entry = getEntry( key, false );
		if( entry != null ){
			entry.removeListener( listener );
			check( entry );
		}
	}
	
	/**
	 * Gets the entry for <code>key</code>.
	 * @param <A> the type of the entry
	 * @param key the name of the entry
	 * @param secure <code>true</code> if <code>null</code> is not a valid 
	 * result. 
	 * @return the entry of <code>null</code>, but only if <code>secure</code>
	 * is <code>false</code>
	 */
	@SuppressWarnings( "unchecked" )
	private <A> Entry<A> getEntry( PropertyKey<A> key, boolean secure ){
		Entry<?> entry = map.get( key );
		if( entry == null && secure ){
			entry = new Entry<A>( key );
			map.put( key, entry );
		}
		return (Entry<A>)entry;
	}
	
	/**
	 * Checks whether <code>entry</code> has to be stored any longer.
	 * @param entry the entry that may be deleted
	 */
	private void check( Entry<?> entry ){
		if( entry.removeable() ){
			map.remove( entry.getKey() );
		}
	}
	
	/**
	 * An entry that contains key, listeners and a value.
	 * @author Benjamin Sigg
	 *
	 * @param <A> the type of the value
	 */
	private class Entry<A>{
		/** the name of this entry */
		private PropertyKey<A> key;
		/** listeners to this entry */
		private List<DockPropertyListener<A>> listeners = new ArrayList<DockPropertyListener<A>>();
		/** the value stored in this entry */
		private A value;
		/** whether the value of this entry has been set by the user */
		private boolean hasBeenSet = false;

		/**
		 * Creates a new entry.
		 * @param key the name of this entry
		 */
		public Entry( PropertyKey<A> key ){
		    this.key = key;
		}
		
		/**
		 * Sets the new value of this entry.
		 * @param value the new value
		 */
		@SuppressWarnings( "unchecked" )
		public void setValue( A value ){
			A oldValue = getValue();
			this.value = value;
			A newValue = getValue();
			
			if( (oldValue == null && newValue != null) ||
				(oldValue != null && newValue == null) ||
				(oldValue != null && !oldValue.equals( newValue ))){
			
				for( DockPropertyListener<A> listener : (DockPropertyListener<A>[])listeners.toArray( new DockPropertyListener<?>[ listeners.size() ] ))
					listener.propertyChanged( DockProperties.this, key, oldValue, newValue );
			}
		}
		
		/**
		 * Gets the value of this entry.
		 * @return the value
		 */
		public A getValue(){
			if( value == null ){
			    if( hasBeenSet )
			        return null;
			    else
			        return key.getDefault();
			}
			return value;
		}
		
		/**
		 * Tells this entry whether the user has set the value.
		 * @param hasBeenSet <code>true</code> if the user changed the value
		 */
		@SuppressWarnings("unchecked")
        public void setHasBeenSet( boolean hasBeenSet ) {
		    A oldValue = getValue();
		    this.hasBeenSet = hasBeenSet;
		    A newValue = getValue();

		    if( (oldValue == null && newValue != null) ||
		            (oldValue != null && newValue == null) ||
		            (oldValue != null && !oldValue.equals( newValue ))){

		        for( DockPropertyListener<A> listener : (DockPropertyListener<A>[])listeners.toArray( new DockPropertyListener<?>[ listeners.size() ] ))
		            listener.propertyChanged( DockProperties.this, key, oldValue, newValue );
		    }
        }
		
		/**
		 * Whether the value of this entry has been set by the user.
		 * @return <code>true</code> if the user changed the value
		 */
		public boolean hasBeenSet() {
            return hasBeenSet;
        }
		
		/**
		 * Gets the name of this entry.
		 * @return the name
		 */
		public PropertyKey<A> getKey(){
			return key;
		}
		
		/**
		 * Adds a new listener to this entry.
		 * @param listener the new listener
		 */
		public void addListener( DockPropertyListener<A> listener ){
			listeners.add( listener );
		}
		
		/**
		 * Removes a listener from this entry.
		 * @param listener the listener to remove
		 */
		public void removeListener( DockPropertyListener<A> listener ){
			listeners.remove( listener );
		}
		
		/**
		 * Tells whether this entry is needed any longer or not.
		 * @return <code>true</code> if this entry can be deleted safely.
		 */
		public boolean removeable(){
			return (!hasBeenSet || key.getDefault() == null) && value == null && listeners.isEmpty();
		}
	}
}
