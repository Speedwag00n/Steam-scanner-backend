package ilia.nemankov.steamscan.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ItemId implements Serializable {

    private static final long serialVersionUID = -6494128798319624418L;

    private long itemId;

    private long gameId;

}
