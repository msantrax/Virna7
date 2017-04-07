package com.opus.virna7;


import java.util.LinkedHashMap;

/**
 * Created by opus on 22/12/16.
 */

public class GaugeType extends EnumItems {

    protected LinkedHashMap<Integer, Gauget> reference;
    private int gaugenum;

    public static enum Gauget {
        EMPTY           ( 0, 0, "Vazio", " ", " ", 0, 0, 0),
        SPEED           ( 1, 1, "Velocidade", "%2.2f rpm", "a Velocidade da Placa", 0, 80, 1),
        TORQUE          ( 2, 1, "Torque", "%2.3f Kgf", "o Arrasto das amostras (Torque)", 630, 830, 2),
        SPIN            ( 3, 1, "Jig Spin", "%2.2f rpm", "a Velocidade de Giro do Suporte", 0, 50, 3),
        SWEEPPOS        ( 4, 1, "Posição do Sweep", "%+2.1f %%", "a Posição do Braço de Sweep", 0, 100, 4),
        WG2SPIN         ( 5, 1, "WG2 Spin", "%+2.2f rpm", "o Giro e Direção do Jig de Polimento", 0, 100, 5),
        MICRONS         ( 6, 1, "Espessura da Amostra", "%2.2f µ", "a Espessura da Amostra", 0, 200, 6),
        DROP            ( 7, 1, "Razão do Abrasivo", "%2.2f seg.", "o Razão do Abrasivo", 0, 200, 7),
        CVEX            ( 8, 1, "Concavidade", "%2.2f µ", "a Concavidade", 0, 3, 8),

        ;

        private String label;
        private String format;
        private String menu;
        private int ordinal;
        private int group;

        private int slot;
        private int vmax;
        private int vmin;



        Gauget (int ordinal, int group, String label, String format, String menu,
                    int vmin, int vmax, int slot

        ){
            this.label=label;
            this.ordinal= ordinal;
            this.format = format;
            this.menu = menu;
            this.group = group;
            this.vmax = vmax;
            this.vmin = vmin;
            this.slot = slot;
        }

        public String getLabel() { return label;}
        public String getFormat() { return format;}
        public String getMenuLabel() { return menu;}

        public Integer getOrdinal() { return ordinal;}
        public Integer getGroup() { return group;}
        public Integer getMax() { return vmax;}
        public Integer getMin() { return vmin;}
        public Integer getSlot() { return slot;}
        //public Integer getGroup() { return group;}

    }

    public GaugeType(){
        super();
        reference = new LinkedHashMap<>();
        gaugenum = 0;

        for (Gauget  stk : Gauget.values()){
            items.put(stk.getLabel(), String.valueOf(stk.getOrdinal()));
            ordinals.put(stk.getOrdinal(), stk.getLabel());
            reference.put(stk.getOrdinal(), stk);
            gaugenum++;
        }
    }

    public int getlenght(){ return gaugenum;}

    public String getFormat(int index){
        return reference.get(index).getFormat();
    }

    public String getMenuEntry(int index){return reference.get(index).getMenuLabel();}

    public int getGroup(int index){
        return reference.get(index).getGroup();
    }

    public int getMax(int index){
        return reference.get(index).getMax();
    }

    public int getMin(int index){
        return reference.get(index).getMin();
    }

    public int getSlot(int index){
        return reference.get(index).getSlot();
    }

    public int getOrdinal(int index){return reference.get(index).getOrdinal();
    }


}
