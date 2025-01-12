package UTILS;

import java.util.ArrayList;


public class Panier {
	
	
	public ArrayList<DetailPanier> articles = new ArrayList<DetailPanier>();

	public Panier() {
		super();
	}

	public Panier(ArrayList<DetailPanier> articles) {
		super();
		this.articles = articles;
	}

	@Override
	public String toString() {
		return "Panier [articles=" + articles + "]";
	}
	
	
	/* ===================================== 
	 * ============== METHODES UTILITAIRES =
	 * =====================================
	*/
	
	//Retourne le nombre d'article dans le panier
	public int nbArt() {
		return articles.size();
	}
	
	
	//Ajouter un produit au panier 
	public void add (DetailPanier detailPanier) {
		
		boolean exist = false;
		
		for( DetailPanier dp : articles) {
			if (dp.getJeux().getId_jeux() == detailPanier.getJeux().getId_jeux()) {
				exist =true;
				dp.setQte(dp.getQte()+detailPanier.getQte());
			}
		}
		
		
		if (!exist) {
			articles.add(detailPanier);
		}
		
	}
	
	
	
	//METHODE POUR CALCULER LE TOTAL
	public double total() {
		double tot = 0;
		
		for(DetailPanier dp :articles) {
			tot += dp.getJeux().getPrix()*dp.getQte();
		}
		
		return tot;
	}
	
	
	
	//METHODE POUR SUPPRIMER
	public void delete(int produitid) {
		DetailPanier detail= new DetailPanier();
		for(DetailPanier pp:articles) {
			if(pp.getJeux().getId_jeux()==produitid) {
				detail=pp;
			}
		}
		articles.remove(detail);
	}
	
	
	//METHODE POUR VIDER
	public void vider() {
		articles= new ArrayList<DetailPanier>();
	}	
	
}
