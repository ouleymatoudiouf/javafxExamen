package sn.ouleymatou.hotelmanagement.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sn.ouleymatou.hotelmanagement.entities.Chambre;
import sn.ouleymatou.hotelmanagement.entities.TypeChambre;
import sn.ouleymatou.hotelmanagement.services.ChambreService;
import sn.ouleymatou.hotelmanagement.services.TypeChambreService;
import sn.ouleymatou.hotelmanagement.utils.Utils;

public class AddChambreDialogController {

    @FXML private ComboBox<TypeChambre> typeComboBox;
    @FXML private ComboBox<Integer> etageComboBox;
    @FXML private CheckBox climCheckBox;
    @FXML private CheckBox balconCheckBox;
    @FXML private CheckBox vueOceanCheckBox;
    @FXML private DatePicker dateDerniereRenovation;

    private final TypeChambreService typeChambreService = new TypeChambreService();
    private final ChambreService chambreService = new ChambreService();

    private Chambre chambreCreeOuModifiee; // chambre créée ou modifiée
    private Chambre chambreModifiee;       // null si ajout, non-null si modification
    private Dialog<Chambre> dialog;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(typeChambreService.getAll()));
        etageComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
    }

    public void setChambre(Chambre chambre) {
        this.chambreModifiee = chambre;
        if (chambre != null) {
            typeComboBox.setValue(chambre.getTypeChambre());
            etageComboBox.setValue(chambre.getEtage());
            climCheckBox.setSelected(chambre.isClimatisation());
            balconCheckBox.setSelected(chambre.isBalcon());
            vueOceanCheckBox.setSelected(chambre.isVueOcean());
            dateDerniereRenovation.setValue(chambre.getDateDerniereRenovation());
        }
    }

    public void setDialog(Dialog<Chambre> dialog) {
        this.dialog = dialog;
        configureResultConverter(dialog);
    }

    private void configureResultConverter(Dialog<Chambre> dialog) {
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK && validerEtEnregistrer()) {
                return chambreCreeOuModifiee;
            }
            return null;
        });
    }

    @FXML
    public void handleOk() {
        if (validerEtEnregistrer()) {
            Utils.showAlert("Succès", chambreModifiee == null ?
                    "Chambre ajoutée avec succès !" :
                    "Chambre modifiée avec succès !");
            if (dialog != null) {
                dialog.setResult(chambreCreeOuModifiee);
                dialog.close();
            }
        }
    }

    public Chambre getChambreCreeOuModifiee() {
        return chambreCreeOuModifiee;
    }

    private boolean validerEtEnregistrer() {
        TypeChambre type = typeComboBox.getValue();
        Integer etage = etageComboBox.getValue();

        if (type == null || etage == null) {
            Utils.showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (chambreModifiee == null) {
            // Création
            String numeroGenere = chambreService.genererNumero(type, etage);
            Chambre nouvelleChambre = new Chambre();
            nouvelleChambre.setNumero(numeroGenere);
            nouvelleChambre.setTypeChambre(type);
            nouvelleChambre.setEtage(etage);
            nouvelleChambre.setClimatisation(climCheckBox.isSelected());
            nouvelleChambre.setBalcon(balconCheckBox.isSelected());
            nouvelleChambre.setVueOcean(vueOceanCheckBox.isSelected());
            nouvelleChambre.setStatut(Chambre.StatutChambre.LIBRE);
            nouvelleChambre.setDateDerniereRenovation(dateDerniereRenovation.getValue());

            chambreService.save(nouvelleChambre);
            chambreCreeOuModifiee = nouvelleChambre;

        } else {
            // Modification
            chambreModifiee.setTypeChambre(type);
            chambreModifiee.setEtage(etage);
            chambreModifiee.setClimatisation(climCheckBox.isSelected());
            chambreModifiee.setBalcon(balconCheckBox.isSelected());
            chambreModifiee.setVueOcean(vueOceanCheckBox.isSelected());
            chambreModifiee.setDateDerniereRenovation(dateDerniereRenovation.getValue());

            try {
                chambreService.modifierChambre(chambreModifiee);
            } catch (IllegalStateException | IllegalArgumentException ex) {
                Utils.showAlert("Erreur", ex.getMessage());
                return false;
            }
            chambreCreeOuModifiee = chambreModifiee;
        }
        return true;
    }
}
