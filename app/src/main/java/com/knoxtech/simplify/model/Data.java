package com.knoxtech.simplify.model;


public class Data {

    String name,email,role,userId,docId,eventName,banner,longDesc,postedBy,organizer,w_group,branch,profilePicture,date,clgName,p_id,certificate,type,proof,rank,payment,api,paymentId;
    public Data() {
    }

    public Data(String name, String email, String role, String userId, String docId, String eventName, String banner, String longDesc, String postedBy, String organizer, String w_group, String branch, String profilePicture, String date, String clgName, String p_id, String certificate, String type, String proof, String rank, String payment,String api,String paymentId) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.docId = docId;
        this.eventName = eventName;
        this.banner = banner;
        this.longDesc = longDesc;
        this.postedBy = postedBy;
        this.organizer = organizer;
        this.w_group = w_group;
        this.branch = branch;
        this.profilePicture = profilePicture;
        this.date = date;
        this.clgName = clgName;
        this.p_id = p_id;
        this.certificate = certificate;
        this.type = type;
        this.proof = proof;
        this.rank = rank;
        this.payment = payment;
        this.api = api;
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getApi() {
        return api;
    }

    public String getPayment() {
        return payment;
    }

    public String getRank() {
        return rank;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getP_id() {
        return p_id;
    }

    public String getClgName() {
        return clgName;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getUserId() {
        return userId;
    }

    public String getDocId() {
        return docId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getBanner() {
        return banner;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getW_group() {
        return w_group;
    }

    public String getBranch() {
        return branch;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

}
