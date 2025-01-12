package UTILS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireJeuUtilisateur {
	
	private int idCommentaire;
    private String contenu;
    private String utilisateurNom;
    private String utilisateurImage;
    private int idJeux; 
    private String jeuTitre;
    private String jeuPhoto;
    private boolean statutCom;
    private boolean acceptation;
}
