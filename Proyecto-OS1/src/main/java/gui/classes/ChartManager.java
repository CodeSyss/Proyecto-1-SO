/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui.classes;

/**
 *
 * @author cehernandez
 */

import main.classes.PCB;
import javax.swing.*;
import java.awt.*;


public class ChartManager extends JPanel implements ListCellRenderer<PCB> {
    
    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel stateLabel;
    private JLabel pcLabel;
    
    public ChartManager() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Organiza verticalmente
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5)); // Margen interior

        nameLabel = new JLabel();
        idLabel = new JLabel();
        stateLabel = new JLabel();
        pcLabel = new JLabel();

        // Estilos básicos (puedes personalizarlos más)
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        idLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        idLabel.setForeground(Color.DARK_GRAY);
        stateLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        pcLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        add(nameLabel);
        add(idLabel);
        add(stateLabel);
        add(pcLabel);
    }
    
        @Override
    public Component getListCellRendererComponent(JList<? extends PCB> list, 
        PCB pcb, int index, 
        boolean isSelected, boolean cellHasFocus) {
                                                      
        // Rellena las etiquetas con los datos del PCB actual
        nameLabel.setText(pcb.getProcessName());
        idLabel.setText("ID: " + pcb.getProcessID_short());
        stateLabel.setText("Estado: " + pcb.getState().toString());
        pcLabel.setText("PC: " + pcb.getProgramCounter() + "/" + pcb.getTotalInstructions());

        // Colores de fondo y texto según si está seleccionado
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        
        // Borde izquierdo coloreado según el estado del proceso
        Color borderColor = Color.GRAY; // Color por defecto
        switch (pcb.getState()) {
            case RUNNING: borderColor = Color.GREEN; break;
            case READY: borderColor = Color.BLUE; break;
            case BLOCKED: borderColor = Color.ORANGE; break;
            case NEW: borderColor = Color.CYAN; break;
            case FINISHED: borderColor = Color.LIGHT_GRAY; break;
            case READY_SUSPENDED: borderColor = new Color(150, 150, 255); break; // Azul pálido
            case BLOCKED_SUSPENDED: borderColor = new Color(255, 200, 150); break; // Naranja pálido
        }
        setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, borderColor)); // Borde izquierdo
        
        setEnabled(list.isEnabled());
        setOpaque(true); // Necesario para que el fondo se pinte

        return this; 
    }

    
    
}
