package com.hangman.server;

import java.util.Random;

public class HangmanGame {

	public class Message {
		int NoOfLetters;
		String correctGuessedLetters;
		int NoOfAttemptsLeft;
		int Score;
	}

	public Message msg = null;
	private String SelectedWord = "";
	private Random rand = null;

	HangmanGame() {
		msg = new Message();
		rand = new Random();
		msg.Score = 0;
	}

	public void selectWord() {
		int listSize = Main.ListOfWords.size();
		int randNum = rand.nextInt(listSize);
		SelectedWord = Main.ListOfWords.get(randNum);
		msg.NoOfLetters = SelectedWord.length();
		msg.NoOfAttemptsLeft = SelectedWord.length();

		String str = "";
		for (int i = 0; i < SelectedWord.length(); i++) {
			str = str.concat("_");
		}
		msg.correctGuessedLetters = str;
		System.out.println("WORD " + SelectedWord);
	}

	public void matchLetter(String letter) {
		boolean MatchFlag = false;
		int index = -1;
		while (index < SelectedWord.length() - 1) {
			index = SelectedWord.indexOf(letter, index + 1);
			if (index == -1) {
				if (MatchFlag == false) {
					updateAttemptsLeft();
					return;
				}
				break;
			} else {
				MatchFlag = true;
				String temp = msg.correctGuessedLetters;
				msg.correctGuessedLetters = "";
				if (index > 0) {
					msg.correctGuessedLetters = msg.correctGuessedLetters.concat(temp.substring(0, index));
				}
				msg.correctGuessedLetters = msg.correctGuessedLetters
						.concat(Character.toString(SelectedWord.charAt(index)));
				if (index < SelectedWord.length() - 1) {
					msg.correctGuessedLetters = msg.correctGuessedLetters
							.concat(temp.substring(index + 1, SelectedWord.length()));
				}
			}
		}
		if (SelectedWord.compareTo(msg.correctGuessedLetters) == 0) {
			msg.NoOfAttemptsLeft = -1;
			msg.Score += 1;
			SelectedWord = "";
			msg.NoOfLetters = 0;

		}
	}

	public void matchWord(String word) {
		if (SelectedWord.compareTo(word) == 0) {
			msg.NoOfAttemptsLeft = -1;
			msg.Score += 1;
			SelectedWord = "";
			msg.NoOfLetters = 0;
			msg.correctGuessedLetters = word;
		} else {
			updateAttemptsLeft();
		}
	}

	public void updateAttemptsLeft() {
		msg.NoOfAttemptsLeft -= 1;
		if (msg.NoOfAttemptsLeft == 0) {
			msg.Score -= 1;
			SelectedWord = "";
			msg.NoOfLetters = 0;
		}
	}

}
