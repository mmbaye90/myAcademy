package Model.ENTITIES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
	private int id_utilisateur;
    private String nom;
    private String email;
    private String password;
    private boolean isAdmin;
    private boolean blacklisted;
    private String image;
}
