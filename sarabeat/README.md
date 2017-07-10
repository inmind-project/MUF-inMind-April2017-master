# WEF-BEAT (Version of InMind Deployment, May 2017)

## Features
- Recommended to run on MacOS: This module should run on both Windows and Mac, but the GUI is optimized to MacOS. 
- BEAT takes text as its input
- BEAT generate JSON formatted behavior plan, namely, BSON (Behavior JSON). 

We have several reasons for this project to use JSON instead of BML. 
- Simplicity: the first version of BSON has simplified functionalities to implement necessary functions faster. It may support other BML functions in the future. 
- Multi-User Framework (MUF) message convention: The MUF recommends JSON as its message format, because of its parsing speed, and the recent web technology trends.
- Ease of implementation: JSON message operation implementation is usually much easier than XML's ones

## Setup
### Stanford CoreNLP
- Download CoreNLP (http://stanfordnlp.github.io/CoreNLP/)
- Run Stanford POS tagger server 

~~~~
java -mx4g -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer 9000
~~~~

### Setup phoneme intervals
In **beat.compiler.BSONCompiler.java**, you can change the interval (currently, 0.2sec for each word)

~~~~
private float timeInterval = 0.2f;
~~~~

## Run
- Make sure the Stanford POS tagger server is running locally
- Start Unity module (https://github.com/mmasley/InMind-April2017-Unity) ... Unity module should be started before launching BEAT. 
- edu.cmu.hcii.articulab.beat.gui.**BEATShell.java**: Right click -> Run As Java Application

## User Interface
### The main user interface.
![GUI1](https://github.com/mmasley/WEF-BEAT/blob/davos2017/doc/images/gui.png)

### Parser tab
Input a sentence on the Sentence box, and click "GENERATE" button.
![GUI2](https://github.com/mmasley/WEF-BEAT/blob/davos2017/doc/images/gui2.png)

### Behavior planning chart tab
All behavior elements are aligned on the time line.
![GUI4](https://github.com/mmasley/WEF-BEAT/blob/davos2017/doc/images/gui4.png)


## SAIBA compatible I/O
### Input for BEAT
~~~~
vrExpress Brad user 1480100642410 <?xml version="1.0" encoding="UTF-8" standalone="no" ?> 
                      <act> <participant id="Brad" role="actor" /><fml> 
                      <turn continuation="false" />
                      <affect type="neutral" target="addressee">
                      </affect> <culture type="neutral"> </culture>
                      <personality type="neutral"> </personality>
                      <sentence phase="greetings" intention="greeting" strategy="NONE" rapport="NONE" text="Hi. My name is SARA." />
                      </fml>
                      <bml>
                      <speech>
                      Hi. My name is SARA.</speech>
                      </bml>
                      <ssml>
                      <speech><s>Hi <break strength="medium"/> I'm Sara <break strength="medium"/> What's <prosody pitch="high">your</prosody> name?</s></speech>
                      </ssml>
                      </act>
~~~~

### Output from BEAT
~~~~~
{
  "speech": "Hi. My name is SARA.",
  "words": [
    {
      "word": "Hi",
      "start_id": 0,
      "end_id": 0,
      "start_time": 0.047,
      "end_time": 0.544,
      "visemes": [
        {
          "startTime": 0.047,
          "symbol": "k",
          "additionalProperties": {}
        },
        {
          "startTime": 0.123,
          "symbol": "a",
          "additionalProperties": {}
        },
        {
          "startTime": 0.394,
          "symbol": "sil",
          "additionalProperties": {}
        }
      ]
    },
    {
      "word": "My",
      "start_id": 0,
      "end_id": 0,
      "start_time": 0.787,
      "end_time": 0.98899996,
      "visemes": [
        {
          "startTime": 0.787,
          "symbol": "p",
          "additionalProperties": {}
        },
        {
          "startTime": 0.839,
          "symbol": "a",
          "additionalProperties": {}
        }
      ]
    },
    {
      "word": "name",
      "start_id": 0,
      "end_id": 0,
      "start_time": 0.951,
      "end_time": 1.3709999,
      "visemes": [
        {
          "startTime": 0.951,
          "symbol": "t",
          "additionalProperties": {}
        },
        {
          "startTime": 1.069,
          "symbol": "e",
          "additionalProperties": {}
        },
        {
          "startTime": 1.221,
          "symbol": "p",
          "additionalProperties": {}
        }
      ]
    },
    {
      "word": "is",
      "start_id": 0,
      "end_id": 0,
      "start_time": 1.305,
      "end_time": 1.546,
      "visemes": [
        {
          "startTime": 1.305,
          "symbol": "i",
          "additionalProperties": {}
        },
        {
          "startTime": 1.396,
          "symbol": "s",
          "additionalProperties": {}
        }
      ]
    },
    {
      "word": "SARA",
      "start_id": 0,
      "end_id": 0,
      "start_time": 1.471,
      "end_time": 1.953,
      "visemes": [
        {
          "startTime": 1.471,
          "symbol": "s",
          "additionalProperties": {}
        },
        {
          "startTime": 1.574,
          "symbol": "E",
          "additionalProperties": {}
        },
        {
          "startTime": 1.61,
          "symbol": "r",
          "additionalProperties": {}
        },
        {
          "startTime": 1.683,
          "symbol": "@",
          "additionalProperties": {}
        },
        {
          "startTime": 1.803,
          "symbol": "sil",
          "additionalProperties": {}
        }
      ]
    }
  ],
  "behaviors": [
    {
      "type": "gaze",
      "start": 0,
      "target": "GazeDown",
      "direction": "up",
      "influence": "head",
      "angle": 4,
      "additionalProperties": {}
    },
    {
      "type": "gaze",
      "start": 1,
      "target": "GazeTarget",
      "direction": "up",
      "influence": "head",
      "angle": 4,
      "additionalProperties": {}
    },
    {
      "type": "gaze",
      "start": 1,
      "target": "GazeTarget",
      "direction": "up",
      "influence": "head",
      "angle": 0,
      "additionalProperties": {}
    },
    {
      "type": "animation",
      "start": 1,
      "name": "beat_middle_right_sara",
      "additionalProperties": {}
    },
    {
      "type": "animation",
      "start": 1,
      "end": 5,
      "amount": 0.2,
      "name": "head_nod_sara",
      "additionalProperties": {}
    },
    {
      "type": "gaze",
      "start": 5,
      "target": "GazeTarget",
      "direction": "up",
      "influence": "head",
      "angle": 0,
      "additionalProperties": {}
    },
    {
      "type": "facs",
      "start": 0,
      "end": 1,
      "au": "105",
      "amount": 0.5,
      "additionalProperties": {}
    }
  ]
}
~~~~
