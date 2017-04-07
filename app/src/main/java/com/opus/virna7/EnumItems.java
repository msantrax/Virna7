package com.opus.virna7;

import java.util.LinkedHashMap;

/**
 * Created by opus on 13/01/17.
 */

public class EnumItems {

    protected LinkedHashMap<String, Object> items;
    protected LinkedHashMap<Integer, String> ordinals;


    protected EnumItems(){
        items = new LinkedHashMap<>();
        ordinals = new LinkedHashMap<>();

    }

    /**
     * Obtem stroke pelo nome
     * @param item
     *  String com o nome
     * @return
     *  Stroke solicitado ou stroke vazio se não identificado
     */
    public <T> T getbyLabel (Class<T> clazz, String item){

        Object obj = items.get(item);
        //return  (obj != null) ? (T) obj : (T)items.get(ordinals.get(0));
        if (obj != null){
            return (T) obj;
        }
        // Item não foi identificado, envie o default mas sinalize  o erro
        //log.log(Level.WARNING, "Label {0} não foi encontrado em ", item);
        return (T)items.get(ordinals.get(0));
    }

    public String[] getLabelsSet() { return (String[])items.keySet().toArray();}

    public String getLabel(Integer ordinal_item) { return ordinals.get(ordinal_item);}

    /**
     * Obtem stroke pelo ordinal
     * @param item
     *  Inteiro com o ordinal de sequencia.
     * @return
     *  Stroke solicitado ou stroke vazio se não identificado
     */
    public <T> T getbyOrdinal (Class<T> clazz, Integer item){

        Object obj = items.get(ordinals.get(item));

        if (obj != null){
            return (T) obj;
        }
        // Item não foi identificado, envie o default mas sinalize  o erro
        //log.log(Level.WARNING, "Ordinal {0} não foi encontrado", item);
        return (T)items.get(ordinals.get(0));
    }

    public Object[] getOrdinalSet() { return ordinals.keySet().toArray();}

    //public Object[] getReferenceSet() { return reference.keySet().toArray();}


}
