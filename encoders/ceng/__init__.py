import subprocess
import pandas as pd
from .utilities import *
from .encoder import Encoder

#### Modify the following to the path where the folder 'ceng' is
path ='../encoders/ceng/'

class CENGEncoder(Encoder):
    """ 
        Methods for the CENG Encoder 
    """ 
    def __init__(self, verbose=1):
        super(CENGEncoder,self).__init__()
        self.verbose = float(verbose)

    def fit(self, df, _): 
        # Calculate bpc
        df.to_csv('temp_input.txt', sep='\t', header=False, index=False)
        if self.verbose == 1.0:
            print(">> Calculating bpc for networks parameters")
        import subprocess
        try: 
            output = subprocess.getoutput(path+'/src/pzip-0.82/pzip temp_input.txt -v')
            bpc = float(output.split('bpc')[0].split('=')[2])
            if self.verbose == 1.0:
                print('>> bpc = ', bpc)
        except: 
            raise Exception('CENG java files unavailable. Change the path in ceng/__init_.py')

        bpc_str = str(bpc)

        # Scale numerical variables to [0,1]
        df = scale_df(df)

        # Take random sample 
        if 10000 < df.shape[0]:
            df = df.sample(frac=0.30, replace=False)
        else:
            if 3000 < df.shape[0]:
                df = df.sample(frac=0.50, replace=False)
        
        # Save file as ceng_input.txt
        df.to_csv('ceng_input.txt', sep='\t', header=False, index=False)

        # Call CENG, which writes the file ceng_codes.txt
        subprocess.call(['java', '-cp', path+'src/', 'CENG', bpc_str, str(self.verbose)])
        # Delete input file
        subprocess.call(['rm', 'ceng_input.txt'])

        # Collect found codes as a dictionary
        aux_file = pd.read_csv('ceng_codes.txt', header=None).iloc[2:][0].values
        self.codes = codes_to_dictionary(aux_file)
        # Delete intermediate files
        subprocess.call(['rm', 'temp_input.txt', 'ceng_codes.txt', 'gmon.out'])

