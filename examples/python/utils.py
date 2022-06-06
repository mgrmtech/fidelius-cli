import re
import os
import uuid


def getFideliusVersion():
    dirname = os.path.dirname(os.path.abspath(__file__))
    with open(os.path.join(dirname, '../../build.gradle')) as f:
        contents = f.read()

    version, *_ = re.findall("\d+\.\d+\.\d+", contents)
    return version


def generateRandomUUID():
    return str(uuid.uuid4())


def ensureDirExists(filePath):
    dirName = os.path.dirname(filePath)
    if (os.path.isdir(dirName)):
        return True
    ensureDirExists(dirName)
    os.mkdir(dirName)
