"""
    A script to parse the output of the comparison script and return a latex tabular object

    @author github.com/erickgrm
"""

import sys
import pandas as pd

filename = ''

def main(argv):
    try:
        if len(argv[0]) == 0:  
            print('Invalid Filename')
            sys.exit(2)
    except: 
        raise Exception('Please pass the name of txt file as argument')

    filename = argv[0]
    name_prefix = filename.split('_re')[0]

    f = open(filename, 'r')
    table = []
    index = []
    encoders = []
    # There are 13 encoders and 8 models
    for i in range(13): #original
        encoder = f.readline().split('\n')[0]
        column = []
        for j in range(8): #Original
            line = f.readline()
            column.append(round(float(line.split(': ')[1].split('\n')[0]),5))
            if i == 0:
                index.append(line.split(': ')[0][1:])

        column.append(np.mean(column))
        encoders.append(encoder)
        table.append(column)
    encoders.append('Average ($\eve(D)$)')

    f.close()

    pd.DataFrame(table, index=encoders, columns=index).transpose().to_latex('./latex/'+name_prefix+'_table.tex')
    print('Latex tabular written to', './latex/'+name_prefix+'_table.tex')

if __name__ == "__main__":
   main(sys.argv[1:])
