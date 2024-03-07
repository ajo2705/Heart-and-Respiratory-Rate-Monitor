/*
Author: Akhil Jose
UID: ajose12
 */
package com.example.ajohearresp.schema;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Map;

@Entity(tableName = "symptoms")
public class HeartRespData {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name="nausea_rating")
    public float nausea;

    @ColumnInfo(name="headache_rating")
    public float headache;

    @ColumnInfo(name="diarrhea_rating")
    public float diarrhea;
    @ColumnInfo(name="soarThroat_rating")
    public float soar_throat;

    @ColumnInfo(name="fever_rating")
    public float fever;

    @ColumnInfo(name="muscleAche_rating")
    public float muscle_ache;

    @ColumnInfo(name="lossOfSmell_rating")
    public float loss_of_smell_or_taste;

    @ColumnInfo(name="cough_rating")
    public float cough;

    @ColumnInfo(name="shortnessOfBreath_rating")
    public float shortness_of_breath;

    @ColumnInfo(name="feelingTired_rating")
    public float feeling_tired;

    @ColumnInfo(name="resp_rate")
    public float respRate;

    @ColumnInfo(name="heart_rate")
    public float heartRate;
    
    public HeartRespData()
    {
        nausea=0.0f;
        headache=0.0f;
        diarrhea=0.0f;
        soar_throat =0.0f;
        fever=0.0f;
        muscle_ache =0.0f;
        loss_of_smell_or_taste =0.0f;
        cough=0.0f;
        shortness_of_breath =0.0f;
        feeling_tired =0.0f;
        respRate=0.0f;
        heartRate=0.0f;
    }
    
    public HeartRespData(float nausea, float headache, float diarrhea, float fever, 
                         float soarThroat, float muscleAche, float lossOfSmell, float cough, 
                         float shortnessOfBreath, float feelingTired, float respRate, 
                         float heartRate)
    {
        this.nausea = nausea;
        this.headache = headache;
        this.diarrhea = diarrhea;
        this.fever = fever;
        this.soar_throat = soarThroat;
        this.muscle_ache = muscleAche;
        this.loss_of_smell_or_taste = lossOfSmell;
        this.cough = cough;
        this.shortness_of_breath = shortnessOfBreath;
        this.feeling_tired = feelingTired;
        this.respRate = respRate;
        this.heartRate = heartRate;
    }

    public HeartRespData(Map<String, Float> data)
    {
        this.nausea = data.getOrDefault("Nausea", 0.0f);
        this.headache = data.getOrDefault("Headache", 0.0f);;
        this.diarrhea = data.getOrDefault("Diarrhea", 0.0f);;
        this.soar_throat = data.getOrDefault("Soar Throat", 0.0f);;
        this.fever = data.getOrDefault("Fever", 0.0f);;
        this.muscle_ache = data.getOrDefault("Muscle Ache", 0.0f);;
        this.loss_of_smell_or_taste = data.getOrDefault("Loss of Smell or Taste", 0.0f);;
        this.cough = data.getOrDefault("Cough", 0.0f);;
        this.shortness_of_breath = data.getOrDefault("Shortness of Breath", 0.0f);;
        this.feeling_tired = data.getOrDefault("Feeling Tired", 0.0f);;
        this.respRate = data.getOrDefault("respRate", 0.0f);;
        this.heartRate = data.getOrDefault("heartRate", 0.0f);;
    }

    public float getNausea(){
        return this.nausea;
    }
    
    public void setNausea(float nausea){
        this.nausea = nausea;
    }
    
    public float getHeadache(){
        return this.headache;
    }

    public void setHeadache(float headache){
        this.headache = headache;
    }
    
    public float getDiarrhea(){
        return this.diarrhea;
    }

    public void setDiarrhea(float diarrhea){
        this.diarrhea = diarrhea;
    }
    
    public float getSoar_throat(){
        return this.soar_throat;
    }

    public void setSoar_throat(float soar_throat){
        this.soar_throat = soar_throat;
    }
    
    public float getMuscle_ache(){
        return this.muscle_ache;
    }

    public void setMuscle_ache(float muscle_ache){
        this.muscle_ache = muscle_ache;
    }
    
    public float getFever(){
        return this.fever;
    }

    public void setFever(float fever){
        this.fever = fever;
    }
    
    public float getLossOfSmell(){
        return this.loss_of_smell_or_taste;
    }

    public void setLossOfSmell(float lossOfSmell){
        this.loss_of_smell_or_taste = lossOfSmell;
    }
    
    public float getCough(){
        return this.cough;
    }

    public void setCough(float cough){
        this.cough = cough;
    }
    
    public float getShortness_of_breath(){
        return this.shortness_of_breath;
    }

    public void setShortness_of_breath(float shortness_of_breath){
        this.shortness_of_breath = shortness_of_breath;
    }
    
    public float getFeeling_tired(){
        return this.feeling_tired;
    }

    public void setFeeling_tired(float feeling_tired){
        this.feeling_tired = feeling_tired;
    }

    public float getRespRate(){
        return this.respRate;
    }

    public void setRespRate(float respRate){
        this.respRate = respRate;
    }

    public float getHeartRate(){
        return this.heartRate;
    }

    public void setHeartRate(float heartRate){
        this.heartRate = heartRate;
    }

    public String toString(){
        return "HeartRate["+ this.uid + " " + getNausea()+" "+ getHeadache()+ " "+ getDiarrhea()+ " "+ getSoar_throat()
                + " "+ getFever()+ " "+ getLossOfSmell()+ " "+ getCough() + " "+ 
                getShortness_of_breath() + " "+ getFeeling_tired() + " "+ getRespRate()+ " "+
                getHeartRate();
    }
}
