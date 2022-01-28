package com.GjigsAI;

import java.nio.file.*;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class Main {

    static String buffered2string(BufferedReader bufferedReader) throws IOException {
        StringBuilder return_string = new StringBuilder();
        String curLine;
        while ((curLine = bufferedReader.readLine()) != null){
            return_string.append(curLine);
        }
        return return_string.toString();
    }

    public static void main(String[] args) throws IOException {

        System.out.print("Enter text file path:");
        BufferedReader input_path = new BufferedReader(
                new InputStreamReader(System.in));

        String file = input_path.readLine(); //file path goes here
        Path path = Paths.get(file);
        BufferedReader bufferedReader = Files.newBufferedReader(path);
        String parse_string = buffered2string(bufferedReader);

        String parse_char = " "; //The character you parse your dataset and input on

        String[] parsed = parse_string.split(parse_char);
        String temp;
        String temp2;
        int max_look = 4; //The maximum for how far the AI can look back.
        int y;
        int i;

        y = parsed.length;

        String[][] database = new String[y][max_look];
        String[] prev = new String[max_look];
        String[] database_answers = new String[y];

        bufferedReader = Files.newBufferedReader(path);
        temp = bufferedReader.readLine();
        for(i = 0; i < max_look; i++){
            prev[i] = parsed[i]; //presets the prev i to be synced
        }

        for (i = max_look+1; i < y; i++){
            temp2 = temp;
            temp = parsed[i];
            database[i][0] = temp2;
            database_answers[i] = temp;

            for (int i2 = 0; i2 < max_look-1; i2++){ // this is a quite slow way to do this
                prev[i2] = prev[i2+1];
            }

            prev[max_look-1] = temp2; //sets up previous things

            for(int i2 = 0; i2 < max_look; i2++){
                database[i][i2] = "";
                for(int i3 = max_look-(i2+1); i3 < max_look; i3++){
                    if(database[i][i2] == ""){
                        database[i][i2] = prev[i3];
                    } else{
                        database[i][i2] = database[i][i2] + " " + prev[i3]; //sets dataset to previous x inputs
                    }
                }
            }
        }

        int top_score;
        int second_score;
        String top_word;
        String second_word;

        System.out.println("Output length:");
        BufferedReader length_input = new BufferedReader(
                new InputStreamReader(System.in));
        int output_length = Integer.parseInt(length_input.readLine());

        System.out.println("Prompt:");
        BufferedReader prompt_input = new BufferedReader(
                new InputStreamReader(System.in));
        String prompt_unparsed = length_input.readLine();

        String[] prompt = prompt_unparsed.split(parse_char);
        String[] output = new String[output_length];
        String[] temp_out = new String[max_look];

        String[] potential_words = new String[600000]; //should be a vector
        int[] word_score = new int[potential_words.length];
        boolean new_word_output;
        int index_carry = 0;
        int value_multiplier = 2; //try whatever values you think is best
        int backup_minimum = 6; //This forces the AI to choose an alternative to the best option to not completely copy the dataset

        int output_index = prompt.length-1;
        int value;
        String answer;

        System.arraycopy(prompt, 0, output, 0, prompt.length);

        //Make this an independent function
        for(i = 0; i<output_length-5; i++){

            Arrays.fill(temp_out, "");
            Arrays.fill(word_score, 0);

            for(int index = 0; index < max_look && output_index-index >= 0; index++){
                for(int index2 = 0; index2 < index+1; index2++){
                    //System.out.println((output_index-index)+index2);
                    if(index2 != 0){
                        temp_out[index] += " " + output[(output_index-index)+index2];
                    }
                    else{
                        temp_out[index] = output[(output_index-index)+index2];
                    }
                }
            }

            //set output array similar to the "prev"  array before\

            for(int i2 = max_look+1; i2 < database_answers.length; i2++){

                //System.out.println(database[i2][0] + " " +output[output_index]);

                if(database[i2][0].equals(output[output_index])){
                    value = 1;
                    for(int i3 = 0; database[i2][i3].equals(temp_out[i3]) && i3 < temp_out.length-1; i3++){
                        value*=value_multiplier;
                    }
                    answer = database_answers[i2];
                    //System.out.println("My answer to '" + temp_out[4] + "' is " + answer);
                    new_word_output = false;

                    for(int i3 = 0; potential_words[i3] != null; i3++){
                        if(potential_words[i3] == answer){
                            potential_words[i3] = answer;
                            word_score[i3]+= value;
                            new_word_output = true;
                        }
                        index_carry = i3+1;
                    }

                    //Checks available spots in the potential words

                    new_word_output = !new_word_output;

                    if(new_word_output){
                        word_score[index_carry] = value;
                        potential_words[index_carry] = answer;
                    }

                }
            }

            top_score = 0;
            top_word = "";

            second_score = 0;
            second_word = "";  //to prevent complete copying of data

            //   System.out.println(Arrays.toString(word_score));

            for(int i2 = 0; i2 < word_score.length && potential_words[i2] != null; i2++){
                if(top_score < word_score[i2]){
                    top_score = word_score[i2];
                    top_word = potential_words[i2];
                } else if(second_score < word_score[i2]){
                    second_score = word_score[i2];
                    second_word = potential_words[i2];
                }
            }

            //System.out.println("Word up");

            if(second_score > (value_multiplier*backup_minimum)-1){
                output[output_index+1] = second_word;
            } else{
                output[output_index+1] = top_word; //You can mess with this some
            }

            output_index++;
        }
        StringBuilder final_output = new StringBuilder();
        for(i = 0; i < output_index+1; i++){
            if(i==0){
                final_output = new StringBuilder(output[i]);
            } else {
                final_output.append(" ").append(output[i]);
            }
        }

        System.out.println(final_output);

    }
}