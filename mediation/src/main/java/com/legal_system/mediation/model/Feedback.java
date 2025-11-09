package com.legal_system.mediation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer feedback_id;

    @ManyToOne
    @JoinColumn(name = "mediator_id")
    private Mediators mediator;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDetails user;

    private Integer rating;

    private String comments;

    // Getters and Setters
    public Integer getFeedback_id() { return feedback_id; }
    public void setFeedback_id(Integer feedback_id) { this.feedback_id = feedback_id; }

    public Mediators getMediator() { return mediator; }
    public void setMediator(Mediators mediator) { this.mediator = mediator; }

    public UserDetails getUser() { return user; }
    public void setUser(UserDetails user) { this.user = user; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
