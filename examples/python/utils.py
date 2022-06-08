import re
import os


def getFideliusVersion():
    dirname = os.path.dirname(os.path.abspath(__file__))
    with open(os.path.join(dirname, '../../build.gradle')) as f:
        gradleBuildContent = f.read()

    version, *_ = re.findall("\d+\.\d+\.\d+", gradleBuildContent)
    return version
