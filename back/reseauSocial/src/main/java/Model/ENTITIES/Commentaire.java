package Model.ENTITIES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commentaire {
	private int id_commentaire;
	private String contenu;
	private boolean statut_com;
	private int id_utilisateur;
	private boolean accepte;
}
