import pandas as pd
import matplotlib.pyplot as plt
import math
import matplotlib
matplotlib.rcParams.update({'font.size': 16})
from scipy.stats import ttest_ind

# homo2vs1
homo2vs1 = pd.DataFrame()
for i in range(1, 41):
	run_file = str(i)+'.stat'
	run = pd.read_csv("statistics/homo.2v1/"+run_file, sep=' ', header=None, index_col=0)
	run = run.iloc[:,0:6].dropna()
	run.index.name = 'gen'
	run.columns = ['mean_dogs', 'bestOfGeneration_dogs', 'bestSoFar_dogs', 'mean_sheep', 'bestOfGeneration_sheep', 'bestSoFar_sheep']
	run = run['bestSoFar_dogs']
	homo2vs1[str(i)] = run
homo2vs1['std_error'] = homo2vs1.std(axis=1) / math.sqrt(40)
homo2vs1['ci_high'] =  (1.96 * homo2vs1['std_error'])
homo2vs1['ci_low'] =  (1.96 * homo2vs1['std_error'])



# hetero2vs1
hetero2vs1 = pd.DataFrame()
for i in range(1, 41):
	run_file = str(i)+'.stat'
	run = pd.read_csv("statistics/hetero.2v1/"+run_file, sep=' ', header=None, index_col=0)
	run = run.iloc[:,0:6].dropna()
	run.index.name = 'gen'
	run.columns = ['mean_dog1', 'bestOfGeneration_dog1', 'bestSoFar_dog1', 'mean_dog2', 'bestOfGeneration_dog2', 'bestSoFar_dog2']
	hetero2vs1[str(i)+"_1"] = run['bestSoFar_dog1']
	hetero2vs1[str(i)+"_2"] = run['bestSoFar_dog2']
hetero2vs1['std_error'] = hetero2vs1.std(axis=1) / math.sqrt(80)
hetero2vs1['ci_high'] =  (1.96 * hetero2vs1['std_error'])
hetero2vs1['ci_low'] =  (1.96 * hetero2vs1['std_error'])

fig, ax = plt.subplots(nrows=1, ncols=1)
ax.errorbar(homo2vs1.index, homo2vs1.mean(axis=1).values, yerr=pd.concat([homo2vs1['ci_low'], 
	homo2vs1['ci_high']], axis=1).T.values, fmt='o-', label='Homo')
ax.errorbar(hetero2vs1.index, hetero2vs1.mean(axis=1).values, yerr=pd.concat([hetero2vs1['ci_low'],hetero2vs1['ci_high']], axis=1).T.values, 
	fmt='o-', label='Hetero')
plt.ylim((0, 70000))
fig.suptitle('Homo vs Hetero - 2vs1 - 40 runs')
legend = ax.legend(loc='lower right', shadow=True)
ax.set_xlabel('Generation')
ax.set_ylabel('Fitness')
fig.tight_layout(pad=2)

## PRINT
plt.savefig("/Users/erotundo/git/evolutionary-shepherding/report/imgs/homo2v1-hetero2v1-bestSoFar.pdf")

## T-TEST
print ttest_ind(homo2vs1.mean(axis=1).values, hetero2vs1.mean(axis=1).values)

