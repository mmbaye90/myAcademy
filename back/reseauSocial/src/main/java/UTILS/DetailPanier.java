package UTILS;

import Model.ENTITIES.Jeux;


public class DetailPanier {
	
	private Jeux jeux;
	private int qte ;
	
	
	public DetailPanier() {
		super();
	}


	public DetailPanier(Jeux jeux, int qte) {
		super();
		this.jeux = jeux;
		this.qte = qte;
	}


	public Jeux getJeux() {
		return jeux;
	}


	public void setJeux(Jeux jeux) {
		this.jeux = jeux;
	}


	public int getQte() {
		return qte;
	}


	public void setQte(int qte) {
		this.qte = qte;
	}


	@Override
	public String toString() {
		return "DetailPanier [jeux=" + jeux + ", qte=" + qte + "]";
	}
	
	
	
	
}
