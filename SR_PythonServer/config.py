import numpy


class Paths:
    # Path of the voice record database
    DATASET_PATH = "./RecDatabase"

    # Path of test voice records in case of testing model offline
    TEST_PATH = "./TestRecs"


class Audio:
    # Sampling rate (Hz)
    samp_rate = 16000

    # Frame size (samples)
    fsize = 400
    # Frame length (seconds)
    flen = fsize / samp_rate

    # Hop size (samples)
    hsize = 160
    # Hop length (seconds)
    hlen = hsize / samp_rate


class Windowing:
    # hamming window
    hamming = lambda x: 0.54 - 0.46 * numpy.cos((2 * numpy.pi * x) / (400 - 1))


class Server:
    # IP Address of the machine
    IP_ADDR = '192.168.0.12'
    # Port number of the machine
    PORT = 6666
