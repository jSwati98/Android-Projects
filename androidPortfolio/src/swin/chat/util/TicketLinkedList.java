package swin.chat.util;

import java.util.Iterator;

public class TicketLinkedList<E> implements Iterable<TicketLinkedList.Ticket<E>>{
	
	private class MyIterator implements Iterator<TicketLinkedList.Ticket<E>>
	{
		private Ticket<E> current = firstTicket;
		private Ticket<E> last = null;

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public TicketLinkedList.Ticket<E> next() {
			last = current;
			current = current.nextTicket;
			return last;
		}

		@Override
		public void remove() {
			last.remove();
		}
		
	}
	
	public static class Ticket<E> implements Comparable<Ticket<E>>
	{
		private double index;
		
		private E element;
		private TicketLinkedList<E> ticketLinkedList;
		
		private Ticket<E> nextTicket,previousTicket;
		
		private Ticket(TicketLinkedList<E> linkedList, E element) {
			this.ticketLinkedList = linkedList;
			this.element = element;
		}
		
		public E getElement() {
			return element;
		}
		
		public void addNext(E element) {
			this.ticketLinkedList.addNext(this, element);
		}
		
		public Ticket<E> getNextTicket() {
			return nextTicket;
		}
		
		public void addPrevious(E element) {
			this.ticketLinkedList.addPrevious(this, element);
		}
		
		public Ticket<E> getPreviousTicket() {
			return previousTicket;
		}
		
		public TicketLinkedList<E> getTicketLinkedList() {
			return ticketLinkedList;
		}
		
		public E remove()
		{
			return this.ticketLinkedList.remove(this);
		}
		
		public boolean isRemoved()
		{
			return this.ticketLinkedList == null;
		}

		@Override
		public int compareTo(Ticket<E> another) {
			if(getTicketLinkedList() != another.getTicketLinkedList())
				throw new IllegalArgumentException();
			
			if(this.index > another.index)
				return 1;
			else if(this.index < another.index)
				return -1;
			else
				return 0;
		}
		
	}
	
	private Ticket<E> firstTicket;
	private Ticket<E> lastTicket;
	
	private int count = 0;
	
	public Ticket<E> add(E element)
	{
		if(isEmpty())
		{

			Ticket<E> t = new Ticket<E>(this, element);
			firstTicket = lastTicket = t;
			count++;
			return t;
		}
		else
		{
			return addNext(lastTicket, element);
		}
	}
	
	public Ticket<E> addNext(Ticket<E> ticket, E next) throws IllegalArgumentException
	{
		if(ticket.getTicketLinkedList() != this)
			throw new IllegalArgumentException();
		
		Ticket<E> nextTicket = new Ticket<E>(this, next);
		
		
		if(ticket.nextTicket != null)
		{
			Ticket<E> nextTicket1 = ticket.nextTicket;
			
			ticket.nextTicket = nextTicket;
			nextTicket.previousTicket = ticket;
			nextTicket.nextTicket = nextTicket1;
			nextTicket1.previousTicket = nextTicket;
			
			nextTicket.index = ticket.index + (nextTicket1.index - ticket.index / 2);
		}
		else if(ticket.nextTicket == null && lastTicket == ticket)
		{
			ticket.nextTicket = nextTicket;
			nextTicket.previousTicket = ticket;
			nextTicket.index = ticket.index + 1;
			lastTicket = nextTicket;
		}
		else
		{
			throw new IllegalStateException();
		}
		count++;
		
		return nextTicket;
	}
	
	public Ticket<E> addPrevious(Ticket<E> ticket, E previous) throws IllegalArgumentException
	{
		if(ticket.getTicketLinkedList() != this)
			throw new IllegalArgumentException();
		
		Ticket<E> previousTicket = new Ticket<E>(this, previous);
		
		
		if(ticket.previousTicket != null)
		{
			Ticket<E> previousTicket1 = ticket.previousTicket;
			
			ticket.previousTicket = previousTicket;
			previousTicket.nextTicket = ticket;
			previousTicket.previousTicket = previousTicket;
			previousTicket1.nextTicket = previousTicket;
			previousTicket.index = previousTicket1.index + (previousTicket.index - previousTicket1.index / 2);
		}
		else if(ticket.previousTicket == null && firstTicket == ticket)
		{
			ticket.previousTicket = previousTicket;
			previousTicket.nextTicket = ticket;
			previousTicket.index = ticket.index;
			if(ticket.nextTicket == null)
				ticket.index++;
			else
				ticket.index = previousTicket.index +  (ticket.nextTicket.index - previousTicket.index / 2);
			firstTicket = previousTicket;
		}
		else
		{
			throw new IllegalStateException();
		}
		
		count++;
		
		return previousTicket;
	}
	
	public E remove(Ticket<E> ticket) throws IllegalArgumentException
	{
		if(ticket.getTicketLinkedList() != this)
			throw new IllegalArgumentException();
		
		if(isEmpty())
			throw new IllegalStateException();
		
		Ticket<E> prevTicket = ticket.getPreviousTicket();
		Ticket<E> nextTicket = ticket.getNextTicket();
		
		if(prevTicket != null && nextTicket != null)
		{
			prevTicket.nextTicket = nextTicket;
			nextTicket.previousTicket = prevTicket;
		}
		else if(prevTicket != null && ticket == lastTicket)
		{
			prevTicket.nextTicket = null;
			lastTicket = prevTicket;
		}
		else if(nextTicket != null && ticket == firstTicket)
		{
			nextTicket.previousTicket = null;
			firstTicket = nextTicket;
		}
		else if(ticket == firstTicket && ticket == lastTicket)
			firstTicket = lastTicket = null;
		else
			throw new IllegalStateException();
		
		ticket.index = -1 * ticket.index;
		ticket.ticketLinkedList = null;
		
		count--;
		return ticket.getElement();
	}
	
	
	public boolean isEmpty()
	{
		return firstTicket == null &&  lastTicket == null && count == 0;
	}
	
	public int size()
	{
		return count;
	}

	public E removeFirst() {
		if(isEmpty())
			throw new IllegalStateException();
		
		return firstTicket.remove();
	}
	
	public E removeLast()
	{
		if(isEmpty())
			throw new IllegalStateException();
		return lastTicket.remove();
	}

	@Override
	public Iterator<Ticket<E>> iterator() {
		return new MyIterator();
	}
	
}
