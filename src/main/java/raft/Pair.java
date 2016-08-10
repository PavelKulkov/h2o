package raft;

public class Pair<TFirst, TSecond> {

    private TFirst first;
    private TSecond second;

    public Pair(TFirst first, TSecond second){
        this.first = first;
        this.second = second;
    }

    public TFirst getFirst(){
        return this.first;
    }

    public TSecond getSecond(){
        return this.second;
    }
}
