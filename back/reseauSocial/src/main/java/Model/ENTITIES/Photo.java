package Model.ENTITIES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
	private int id_photo;
    private String url_photo;
    private int id_jeux;
}
