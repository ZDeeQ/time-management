package com.example.time;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class GoalAdapterSimpleTwo extends RecyclerView.Adapter<GoalAdapterSimpleTwo.ViewHolder>{
    private List<Goal> goalList;
    private int position;
    static class ViewHolder extends RecyclerView.ViewHolder{
        View goalView;
        ImageView goalImageSimple;
        TextView goalNameSimple;

        public ViewHolder(View view) {
            super(view);
            goalView=view;
            goalImageSimple=(ImageView)view.findViewById(R.id.goal_image_simple);
            goalNameSimple=(TextView)view.findViewById(R.id.goal_name_simple);
        }
    }

    public GoalAdapterSimpleTwo(List<Goal> goalList){
        this.goalList=goalList;
    }
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.goal_item_simple,parent,false);
        final ViewHolder viewHolder=new GoalAdapterSimpleTwo.ViewHolder(view);
        viewHolder.goalView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                position=(int)view.getTag();
                TomatoFragment.goalText.setText(goalList.get(position).getName());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GoalAdapterSimpleTwo.ViewHolder holder, int position) {
        Goal goal=goalList.get(position);
        holder.goalImageSimple.setImageResource((goal.getGoalImageID()));
        holder.goalNameSimple.setText(goal.getName());

        holder.goalView.setTag(position);
    }

    public int getItemCount() {
        return goalList.size();
    }

    public int getPosition() {
        return position;
    }
}